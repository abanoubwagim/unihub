package com.unihub.identity.application.impl;

import com.unihub.identity.api.dto.VerifyEmailRequest;
import com.unihub.identity.application.usecase.VerifyEmailUseCase;
import com.unihub.identity.domain.model.EmailVerificationToken;
import com.unihub.identity.domain.model.User;
import com.unihub.identity.domain.repository.EmailVerificationTokenRepository;
import com.unihub.identity.domain.repository.UserRepository;
import com.unihub.shared.exception.BadRequestException;
import com.unihub.shared.exception.NotFoundException;
import com.unihub.shared.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class VerifyEmailUseCaseImpl implements VerifyEmailUseCase {

    private static final int MAX_ATTEMPTS = 5;

    private final UserRepository userRepository;
    private final EmailVerificationTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void verifyEmail(VerifyEmailRequest request) {

        String email = request.email().trim().toLowerCase();

        // Get user
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));

        // Already verified
        if (user.isEmailVerified()) {
            throw new BadRequestException("Email is already verified");
        }

        // Get token
        EmailVerificationToken token = tokenRepository.findByUserId(user.getId())
                .orElseThrow(() -> new BadRequestException("No verification code found. Please request a new one"));

        // Check if already used
        if (token.isUsed()) {
            throw new BadRequestException("Verification code already used. Please request a new one");
        }

        // Check expiry
        if (token.isExpired()) {
            throw new BadRequestException("Verification code has expired. Please request a new one");
        }

        // Check max attempts
        if (token.getAttempts() >= MAX_ATTEMPTS) {
            throw new UnauthorizedException("Too many attempts. Please request a new verification code");
        }

        // Increment attempts before checking
        token.incrementAttempts();

        // Check OTP
        if (!passwordEncoder.matches(request.otp(), token.getOtpHash())) {
            tokenRepository.save(token);
            throw new BadRequestException("Invalid verification code");
        }

        // Mark token as used
        token.markUsed();
        tokenRepository.save(token);

        // Verify user
        user.verifyEmail();
        user.activate();
        userRepository.save(user);
    }
}