package com.unihub.identity.application.impl;

import com.unihub.identity.api.dto.ForgotPasswordRequest;
import com.unihub.identity.application.event.PasswordResetRequestedEvent;
import com.unihub.identity.application.usecase.ForgotPasswordUseCase;
import com.unihub.identity.domain.config.IdentityConstants;
import com.unihub.identity.domain.enums.AuthProvider;
import com.unihub.identity.domain.model.PasswordResetToken;
import com.unihub.identity.domain.repository.PasswordResetTokenRepository;
import com.unihub.identity.domain.repository.UserRepository;
import com.unihub.shared.exception.BadRequestException;
import com.unihub.shared.util.OtpGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ForgotPasswordUseCaseImpl implements ForgotPasswordUseCase {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {

        String email = request.email().trim().toLowerCase();

        userRepository.findByEmail(email).ifPresent(user -> {

            if (user.getAuthProvider() != AuthProvider.LOCAL) {
                log.debug("Password reset ignored for OAuth account — userId={}", user.getId());
                return;
            }
            if (!user.isEmailVerified()) {
                log.debug("Password reset ignored for unverified account — userId={}", user.getId());
                return;
            }

            try {
                processReset(user.getId(), email);
            } catch (BadRequestException e) {

                log.warn("Password reset suppressed (rate limit or concurrent request) — userId={}", user.getId());
            }
        });
    }

    private void processReset(UUID userId, String email) {
        String otp = OtpGenerator.generate();
        String otpHash = passwordEncoder.encode(otp);
        LocalDateTime now = LocalDateTime.now();

        PasswordResetToken token = tokenRepository.findByUserId(userId)
                .map(existing -> {
                    enforceRateLimit(existing);
                    existing.resetFor(otpHash);
                    return existing;
                })
                .orElseGet(() -> PasswordResetToken.builder()
                        .userId(userId)
                        .otpHash(otpHash)
                        .expiresAt(now.plusMinutes(IdentityConstants.OTP_EXPIRY_MINUTES))
                        .createdAt(now)
                        .used(false)
                        .attempts(0)
                        .build());

        tokenRepository.save(token);
        log.debug("Password reset token generated — userId={}", userId);
        eventPublisher.publishEvent(new PasswordResetRequestedEvent(userId, email, otp));
    }

    private void enforceRateLimit(PasswordResetToken token) {
        LocalDateTime cooldownBoundary = LocalDateTime.now().minusMinutes(IdentityConstants.RATE_LIMIT_MINUTES);
        if (token.getCreatedAt().isAfter(cooldownBoundary)) {
            throw new BadRequestException("Please wait before requesting a new code");
        }
    }
}