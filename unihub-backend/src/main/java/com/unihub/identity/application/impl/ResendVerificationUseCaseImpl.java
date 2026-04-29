package com.unihub.identity.application.impl;


import java.time.LocalDateTime;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.unihub.identity.api.dto.ResendVerificationRequest;
import com.unihub.identity.application.event.EmailVerificationRequestedEvent;
import com.unihub.identity.application.usecase.ResendVerificationUseCase;
import com.unihub.identity.domain.model.User;
import com.unihub.identity.domain.repository.EmailVerificationTokenRepository;
import com.unihub.identity.domain.repository.UserRepository;
import com.unihub.shared.exception.BadRequestException;
import com.unihub.shared.util.OtpGenerator;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ResendVerificationUseCaseImpl implements ResendVerificationUseCase {

    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final EmailVerificationTokenRepository tokenRepository;

    @Override
    @Transactional
    public void resendVerification(ResendVerificationRequest request) {

        String email = request.email().trim().toLowerCase();

        User user = userRepository.findByEmail(email)
                .orElseThrow(
                        () -> new BadRequestException("If this email is registered, a verification code will be sent"));

        if (user.isEmailVerified()) {
            throw new BadRequestException("Email is already verified");
        }

        tokenRepository.findByUserId(user.getId()).ifPresent(existing -> {
            if (existing.getCreatedAt().isAfter(LocalDateTime.now().minusMinutes(1))) {
                throw new BadRequestException("Please wait before requesting a new code");
            }
        });

        String otp = OtpGenerator.generate();

        eventPublisher.publishEvent(
                new EmailVerificationRequestedEvent(user.getId(), user.getEmail(), otp));
    }


}
