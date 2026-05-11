package com.unihub.identity.infrastructure.messaging;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.unihub.identity.application.event.PasswordResetRequestedEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class PasswordResetEventListener {

    private final JavaMailSender mailSender;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePasswordReset(PasswordResetRequestedEvent event) {
        sendResetEmail(event.email(), event.otp());
    }

    private void sendResetEmail(String email, String otp) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject("UniHub — Password Reset Code");
            message.setText("""
                    Hi,

                    You requested to reset your UniHub password.

                    Your reset code is: %s

                    This code expires in 5 minutes.
                    If you did not request this, please ignore this email.

                    """.formatted(otp));
            mailSender.send(message);
            log.info("Password reset email sent — to={}", email);
        } catch (Exception e) {
            log.error("Failed to send password reset email — to={}, error={}", email, e.getMessage());
        }
    }
}