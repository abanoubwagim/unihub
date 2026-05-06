package com.unihub.identity.application.impl;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.unihub.identity.api.dto.ForgotPasswordRequest;
import com.unihub.identity.application.usecase.ForgotPasswordUseCase;
import com.unihub.identity.domain.model.PasswordResetToken;
import com.unihub.identity.domain.repository.PasswordResetTokenRepository;
import com.unihub.identity.domain.repository.UserRepository;
import com.unihub.shared.exception.BadRequestException;
import com.unihub.shared.util.OtpGenerator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ForgotPasswordUseCaseImpl implements ForgotPasswordUseCase {

    private static final int RATE_LIMIT_MINUTES = 1;
    private static final int OTP_EXPIRY_MINUTES = 5;

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final ForgotPasswordEmailSender emailSender;

    @Override
    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {

        String email = request.email().trim().toLowerCase();

        // Silently ignore unknown e-mails to avoid user-enumeration
        userRepository.findByEmail(email).ifPresent(user -> processReset(user.getId(), email));
    }

    private void processReset(UUID userId, String email) {

        String otp = OtpGenerator.generate();
        String otpHash = passwordEncoder.encode(otp);

        Optional<PasswordResetToken> existing = tokenRepository.findByUserId(userId);

        if (existing.isPresent()) {
            enforceRateLimit(existing.get());
            existing.get().resetFor(otpHash);
            tokenRepository.save(existing.get());
            log.debug("Password reset token refreshed for userId={}", userId);

        } else {
            PasswordResetToken token = PasswordResetToken.builder()
                    .id(UUID.randomUUID())
                    .userId(userId)
                    .otpHash(otpHash)
                    .expiresAt(LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES))
                    .used(false)
                    .attempts(0)
                    .createdAt(LocalDateTime.now())
                    .build();
            tokenRepository.save(token);
            log.debug("New password reset token created for userId={}", userId);
        }

        // Fire-and-forget — runs in the caller's thread pool via @Async
        emailSender.sendResetEmail(email, otp);
    }

    private void enforceRateLimit(PasswordResetToken token) {
        LocalDateTime cooldownBoundary = LocalDateTime.now().minusMinutes(RATE_LIMIT_MINUTES);
        if (token.getCreatedAt().isAfter(cooldownBoundary)) {
            throw new BadRequestException("Please wait before requesting a new code");
        }
    }
}