package com.unihub.identity.application.impl;

import com.unihub.identity.api.dto.VerifyEmailRequest;
import com.unihub.identity.application.usecase.VerifyEmailUseCase;
import com.unihub.identity.domain.config.IdentityConstants;
import com.unihub.identity.domain.model.EmailVerificationToken;
import com.unihub.identity.domain.model.User;
import com.unihub.identity.domain.repository.EmailVerificationTokenRepository;
import com.unihub.identity.domain.repository.UserRepository;
import com.unihub.shared.exception.BadRequestException;
import com.unihub.shared.exception.NotFoundException;
import com.unihub.shared.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class VerifyEmailUseCaseImpl implements VerifyEmailUseCase {

    private final UserRepository userRepository;
    private final EmailVerificationTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(noRollbackFor = BadRequestException.class)
    public void verifyEmail(VerifyEmailRequest request) {
        String email = request.email().trim().toLowerCase();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (user.isEmailVerified()) {
            throw new BadRequestException("Email is already verified");
        }

        EmailVerificationToken token = tokenRepository.findByUserId(user.getId())
                .orElseThrow(() -> new BadRequestException(
                        "No verification code found. Please request a new one"));

        if (token.isUsed()) {
            throw new BadRequestException("Verification code already used. Please request a new one");
        }

        if (token.isExpired()) {
            log.warn("Email verification failed — token expired — userId={}", user.getId());
            throw new BadRequestException("Verification code has expired. Please request a new one");
        }

        if (token.getAttempts() >= IdentityConstants.MAX_OTP_ATTEMPTS) {
            log.warn("Email verification locked — max attempts reached — userId={}", user.getId());
            throw new UnauthorizedException("Too many attempts. Please request a new verification code");
        }

        token.incrementAttempts();
        tokenRepository.save(token);

        if (!passwordEncoder.matches(request.otp(), token.getOtpHash())) {
            log.warn("Email verification failed — wrong OTP — userId={}, attempt={}",
                    user.getId(), token.getAttempts());
            throw new BadRequestException("Invalid verification code");
        }

        token.markUsed();
        tokenRepository.save(token);
        user.verifyEmail();
        user.activate();
        userRepository.save(user);

        log.info("Email verified successfully — userId={}", user.getId()); 
    }
}