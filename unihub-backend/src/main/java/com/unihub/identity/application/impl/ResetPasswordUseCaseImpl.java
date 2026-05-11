package com.unihub.identity.application.impl;

import com.unihub.identity.api.dto.ResetPasswordRequest;
import com.unihub.identity.application.usecase.ResetPasswordUseCase;
import com.unihub.identity.domain.model.PasswordResetToken;
import com.unihub.identity.domain.model.User;
import com.unihub.identity.domain.repository.PasswordResetTokenRepository;
import com.unihub.identity.domain.repository.UserRepository;
import com.unihub.shared.exception.BadRequestException;
import com.unihub.shared.security.TokenBlacklistService;
import com.unihub.shared.util.TokenHashUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResetPasswordUseCaseImpl implements ResetPasswordUseCase {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenBlacklistService tokenBlacklistService;

    @Value("${jwt.expiration:86400000}")
    private long jwtExpirationMs;

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {

        if (!request.newPassword().equals(request.confirmPassword())) {
            throw new BadRequestException("Passwords do not match");
        }

        String hashedToken = TokenHashUtil.sha256(request.resetToken());

        PasswordResetToken token = tokenRepository.findByResetToken(hashedToken)
                .orElseThrow(() -> {
                    log.warn("Password reset failed — token not found or already used");
                    return new BadRequestException("Invalid or expired reset token");
                });

        if (token.isResetTokenExpired()) {
            log.warn("Password reset failed — token expired — userId={}", token.getUserId());
            throw new BadRequestException("Reset token has expired. Please start over");
        }

        User user = userRepository.findById(token.getUserId())
                .orElseThrow(() -> {
                    log.error("Password reset failed — user not found for valid token — userId={}",
                            token.getUserId());
                    return new BadRequestException("User not found");
                });

        user.changePassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);

        tokenRepository.deleteByUserId(token.getUserId());

        long nowEpoch = Instant.now().getEpochSecond();
        long ttlSeconds = jwtExpirationMs / 1000;
        tokenBlacklistService.invalidateAllTokensBefore(
                user.getId().toString(), nowEpoch, ttlSeconds);

        log.info("Password reset successful — all sessions invalidated — userId={}", user.getId());
    }
}