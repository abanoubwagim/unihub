package com.unihub.identity.application.impl;

import java.time.LocalDateTime;
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

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final ForgotPasswordEmailSender emailSender;

    @Override
    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        String email = request.email().trim().toLowerCase();

        userRepository.findByEmail(email).ifPresent(user -> {

            String otp = OtpGenerator.generate();
            String otpHash = passwordEncoder.encode(otp);

            tokenRepository.findByUserId(user.getId()).ifPresentOrElse(
                    existing -> {
                        // Rate limit
                        if (existing.getCreatedAt().isAfter(LocalDateTime.now().minusMinutes(1))) {
                            throw new BadRequestException("Please wait before requesting a new code");
                        }
                        // Edit the existing one instead of deleting and creating a new one
                        existing.resetFor(otpHash);
                        tokenRepository.save(existing);
                    },
                    () -> {
                        
                        // No token — New work
                        PasswordResetToken token = PasswordResetToken.builder()
                                .id(UUID.randomUUID())
                                .userId(user.getId())
                                .otpHash(otpHash)
                                .expiresAt(LocalDateTime.now().plusMinutes(5))
                                .used(false)
                                .attempts(0)
                                .createdAt(LocalDateTime.now())
                                .build();
                        tokenRepository.save(token);
                    });

            // Delegate to separate 
            emailSender.sendResetEmail(email, otp);
        });
    }
}
