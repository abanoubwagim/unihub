package com.unihub.identity.application;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.unihub.identity.application.event.EmailVerificationRequestedEvent;
import com.unihub.identity.domain.EmailVerificationToken;
import com.unihub.identity.domain.EmailVerificationTokenRepository;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailVerificationEventListener {

    private final EmailVerificationTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleEmailVerification(EmailVerificationRequestedEvent event) {

        // Delete old token if exists
        tokenRepository.deleteByUserId(event.userId());

        // Hash the OTP before saving
        String otpHash = passwordEncoder.encode(event.otp());

        // Save new token
        EmailVerificationToken token = EmailVerificationToken.builder()
                .id(UUID.randomUUID())
                .userId(event.userId())
                .otpHash(otpHash)
                .expiresAt(LocalDateTime.now().plusMinutes(5))
                .used(false)
                .attempts(0)
                .createdAt(LocalDateTime.now())
                .build();

        tokenRepository.save(token);

        // Send email
        sendVerificationEmail(event.email(), event.otp());
    }

    private void sendVerificationEmail(String email, String otp) {

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject("UniHub — Verify Your Email");
            message.setText("""
                    Welcome to UniHub!

                    Your verification code is: %s

                    This code expires in 5 minutes.

                    If you did not create an account, please ignore this email.
                    """.formatted(otp));
            mailSender.send(message);
            log.info("Verification email sent to {}", email);
        } catch (Exception e) {
            log.error("Failed to send verification email to {}: {}", email, e.getMessage());
        }
    }

}
