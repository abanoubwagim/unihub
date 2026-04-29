package com.unihub.identity.application;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.unihub.identity.api.dto.ResetPasswordRequest;
import com.unihub.identity.domain.PasswordResetToken;
import com.unihub.identity.domain.PasswordResetTokenRepository;
import com.unihub.identity.domain.User;
import com.unihub.identity.domain.UserRepository;
import com.unihub.shared.exception.BadRequestException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ResetPasswordService implements ResetPasswordUseCase {

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
        PasswordResetToken token = tokenRepository.findByResetToken(request.resetToken())
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