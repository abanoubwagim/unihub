package com.unihub.identity.application.impl;

import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.unihub.identity.api.dto.VerifyResetOtpRequest;
import com.unihub.identity.api.dto.VerifyResetOtpResponse;
import com.unihub.identity.application.usecase.VerifyResetOtpUseCase;
import com.unihub.identity.domain.model.PasswordResetToken;
import com.unihub.identity.domain.model.User;
import com.unihub.identity.domain.repository.PasswordResetTokenRepository;
import com.unihub.identity.domain.repository.UserRepository;
import com.unihub.shared.exception.BadRequestException;
import com.unihub.shared.exception.NotFoundException;
import com.unihub.shared.exception.UnauthorizedException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VerifyResetOtpUseCaseImpl implements VerifyResetOtpUseCase {

    private static final int MAX_ATTEMPTS = 5;

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public VerifyResetOtpResponse verifyResetOtp(VerifyResetOtpRequest request) {

        String email = request.email().trim().toLowerCase();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));

        PasswordResetToken token = tokenRepository.findByUserId(user.getId())
                .orElseThrow(() -> new BadRequestException("No reset code found. Please request a new one"));

        if (token.isUsed()) {
            throw new BadRequestException("Reset code already used. Please request a new one");
        }

        if (token.isExpired()) {
            throw new BadRequestException("Reset code has expired. Please request a new one");
        }

        if (token.getAttempts() >= MAX_ATTEMPTS) {
            throw new UnauthorizedException("Too many attempts. Please request a new reset code");
        }

        token.incrementAttempts();

        if (!passwordEncoder.matches(request.otp(), token.getOtpHash())) {
            tokenRepository.save(token);
            throw new BadRequestException("Invalid reset code");
        }

        // OTP is correct — generate reset token
        String resetToken = UUID.randomUUID().toString();
        token.setResetToken(passwordEncoder.encode(resetToken));
        token.markUsed();
        tokenRepository.save(token);

        return new VerifyResetOtpResponse(resetToken);
    }
}