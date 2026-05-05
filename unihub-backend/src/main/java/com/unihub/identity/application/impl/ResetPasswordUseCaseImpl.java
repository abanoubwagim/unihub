package com.unihub.identity.application.impl;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.unihub.identity.api.dto.ResetPasswordRequest;
import com.unihub.identity.application.usecase.ResetPasswordUseCase;
import com.unihub.identity.domain.model.PasswordResetToken;
import com.unihub.identity.domain.model.User;
import com.unihub.identity.domain.repository.PasswordResetTokenRepository;
import com.unihub.identity.domain.repository.UserRepository;
import com.unihub.shared.exception.BadRequestException;
import com.unihub.shared.util.TokenHashUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ResetPasswordUseCaseImpl implements ResetPasswordUseCase {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {

        if (!request.newPassword().equals(request.confirmPassword())) {
            throw new BadRequestException("Passwords do not match");
        }

        // Find the token using resetToken
        String hashedToken = TokenHashUtil.sha256(request.resetToken());
        
        PasswordResetToken token = tokenRepository.findByResetToken(hashedToken)
                .orElseThrow(() -> new BadRequestException("Invalid or expired reset token"));

        if (token.isResetTokenExpired()) {
            throw new BadRequestException("Reset token has expired. Please start over");
        }

        User user = userRepository.findById(token.getUserId())
                .orElseThrow(() -> new BadRequestException("User not found"));

        user.changePassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);

        // Delete the token after use
        tokenRepository.deleteByUserId(token.getUserId());
    }
}