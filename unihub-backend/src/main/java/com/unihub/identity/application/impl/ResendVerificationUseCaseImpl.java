package com.unihub.identity.application.impl;

import com.unihub.identity.api.dto.req.ResendVerificationRequest;
import com.unihub.identity.application.event.EmailVerificationRequestedEvent;
import com.unihub.identity.application.usecase.ResendVerificationUseCase;
import com.unihub.identity.domain.config.IdentityConstants;
import com.unihub.identity.domain.model.EmailVerificationToken;
import com.unihub.identity.domain.model.User;
import com.unihub.identity.domain.repository.EmailVerificationTokenRepository;
import com.unihub.identity.domain.repository.UserRepository;
import com.unihub.shared.exception.BadRequestException;
import com.unihub.shared.util.OtpGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResendVerificationUseCaseImpl implements ResendVerificationUseCase {

    private final UserRepository userRepository;
    private final EmailVerificationTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public void resendVerification(ResendVerificationRequest request) {
        String email = request.email().trim().toLowerCase();

        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty())
            return;

        User user = userOpt.get();
        if (user.isEmailVerified())
            return;

        Optional<EmailVerificationToken> existingOpt = tokenRepository.findByUserId(user.getId());

        existingOpt.ifPresent(existing -> {
            if (existing.getCreatedAt()
                    .isAfter(LocalDateTime.now().minusMinutes(IdentityConstants.RATE_LIMIT_MINUTES))) {
                throw new BadRequestException("Please wait before requesting a new code");
            }
        });

        String otp = OtpGenerator.generate();
        String otpHash = passwordEncoder.encode(otp);

        EmailVerificationToken token;
        if (existingOpt.isPresent()) {
            token = existingOpt.get();
            token.refresh(otpHash, IdentityConstants.OTP_EXPIRY_MINUTES);
        } else {
            token = EmailVerificationToken.builder()
                    .userId(user.getId())
                    .otpHash(otpHash)
                    .expiresAt(LocalDateTime.now().plusMinutes(IdentityConstants.OTP_EXPIRY_MINUTES))
                    .used(false)
                    .attempts(0)
                    .createdAt(LocalDateTime.now())
                    .build();
        }

        try {
            tokenRepository.save(token);
        } catch (OptimisticLockingFailureException e) {
            log.warn("Concurrent resend-verification (version conflict) — userId={}", user.getId());
            throw new BadRequestException("Please wait before requesting a new code");
        } catch (DataIntegrityViolationException e) {
            log.warn("Concurrent resend-verification (insert conflict) — userId={}", user.getId());
            throw new BadRequestException("Please wait before requesting a new code");
        }


        eventPublisher.publishEvent(
                new EmailVerificationRequestedEvent(user.getId(), user.getEmail(), otp));

        log.info("Verification email re-queued — userId={}", user.getId());
    }
}