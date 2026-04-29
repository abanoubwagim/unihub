package com.unihub.identity.application;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.unihub.identity.api.dto.ForgotPasswordRequest;
import com.unihub.identity.domain.PasswordResetToken;
import com.unihub.identity.domain.PasswordResetTokenRepository;
import com.unihub.identity.domain.UserRepository;
import com.unihub.shared.exception.BadRequestException;
import com.unihub.shared.util.OtpGenerator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ForgotPasswordService implements ForgotPasswordUseCase {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;

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
                        // عدّل الموجود بدل ما تمسح وتعمل جديد
                        existing.resetFor(otpHash);
                        tokenRepository.save(existing);
                    },
                    () -> {
                        // مفيش token — عمل جديد
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

            sendResetEmail(email, otp);
        });
    }

    @Async
    protected void sendResetEmail(String email, String otp) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject("UniHub — Reset Your Password");
            message.setText("""
                    You requested a password reset.

                    Your reset code is: %s

                    This code expires in 5 minutes.

                    If you did not request this, please ignore this email.
                    """.formatted(otp));
            mailSender.send(message);
            log.info("Password reset email sent to {}", email);
        } catch (Exception e) {
            log.error("Failed to send reset email to {}: {}", email, e.getMessage());
        }
    }
}
