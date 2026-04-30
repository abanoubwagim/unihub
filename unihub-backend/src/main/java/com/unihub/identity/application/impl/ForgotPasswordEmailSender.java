package com.unihub.identity.application.impl;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class ForgotPasswordEmailSender {

    private final JavaMailSender mailSender;

    @Async
    public void sendResetEmail(String email, String otp) {
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