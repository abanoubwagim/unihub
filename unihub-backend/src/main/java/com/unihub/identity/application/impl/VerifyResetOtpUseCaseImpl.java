package com.unihub.identity.application.impl;

import com.unihub.identity.api.dto.VerifyResetOtpRequest;
import com.unihub.identity.api.dto.VerifyResetOtpResponse;
import com.unihub.identity.application.usecase.VerifyResetOtpUseCase;
import com.unihub.identity.domain.config.IdentityConstants;
import com.unihub.identity.domain.model.PasswordResetToken;
import com.unihub.identity.domain.model.User;
import com.unihub.identity.domain.repository.PasswordResetTokenRepository;
import com.unihub.identity.domain.repository.UserRepository;
import com.unihub.shared.exception.BadRequestException;
import com.unihub.shared.exception.UnauthorizedException;
import com.unihub.shared.util.TokenHashUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class VerifyResetOtpUseCaseImpl implements VerifyResetOtpUseCase {

    private static final String GENERIC_NOT_FOUND_MSG = "No reset code found for this email. Please request a new one";

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(noRollbackFor = BadRequestException.class)
    public VerifyResetOtpResponse verifyResetOtp(VerifyResetOtpRequest request) {
        String email = request.email().trim().toLowerCase();

        User user = userRepository.findByEmail(email).orElse(null);
        PasswordResetToken token = (user == null)
                ? null
                : tokenRepository.findByUserId(user.getId()).orElse(null);

        if (user == null || token == null) {
            log.warn("Reset OTP verify — no token found for email hash={}",
                    email.hashCode()); // hash only — no plain email in logs
            throw new BadRequestException(GENERIC_NOT_FOUND_MSG);
        }

        if (token.isUsed()) {
            log.warn("Reset OTP verify — token already used — userId={}", user.getId());
            throw new BadRequestException("Reset code already used. Please request a new one");
        }

        if (token.isExpired()) {
            log.warn("Reset OTP verify — token expired — userId={}", user.getId());
            throw new BadRequestException("Reset code has expired. Please request a new one");
        }

        if (token.getAttempts() >= IdentityConstants.MAX_OTP_ATTEMPTS) {
            log.warn("Reset OTP verify — max attempts reached — userId={}", user.getId());
            throw new UnauthorizedException("Too many attempts. Please request a new reset code");
        }

        token.incrementAttempts();
        tokenRepository.save(token);

        if (!passwordEncoder.matches(request.otp(), token.getOtpHash())) {
            log.warn("Reset OTP verify — wrong OTP — userId={}, attempt={}",
                    user.getId(), token.getAttempts());
            throw new BadRequestException("Invalid reset code");
        }

        String plainResetToken = UUID.randomUUID().toString();
        String hashedResetToken = TokenHashUtil.sha256(plainResetToken);
        token.setResetToken(hashedResetToken);
        token.markUsed();
        tokenRepository.save(token);

        log.info("Reset OTP verified successfully — userId={}", user.getId());
        return new VerifyResetOtpResponse(plainResetToken);
    }
}