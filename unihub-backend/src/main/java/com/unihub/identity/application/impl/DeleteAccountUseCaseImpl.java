package com.unihub.identity.application.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.unihub.identity.application.usecase.DeleteAccountUseCase;
import com.unihub.identity.domain.enums.AuthProvider;
import com.unihub.identity.domain.model.User;
import com.unihub.identity.domain.repository.EmailVerificationTokenRepository;
import com.unihub.identity.domain.repository.PasswordResetTokenRepository;
import com.unihub.identity.domain.repository.UserRepository;
import com.unihub.shared.config.RabbitMqConfig;
import com.unihub.shared.events.UserDeletedEvent;
import com.unihub.shared.exception.BadRequestException;
import com.unihub.shared.exception.NotFoundException;
import com.unihub.shared.outbox.OutboxMessage;
import com.unihub.shared.outbox.OutboxMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeleteAccountUseCaseImpl implements DeleteAccountUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final OutboxMessageRepository outboxMessageRepository;
    private final ObjectMapper objectMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public void deleteAccount(UUID userId, String password) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (user.getAuthProvider() != AuthProvider.LOCAL) {
            throw new BadRequestException(
                    "OAuth accounts (" + user.getAuthProvider().name().toLowerCase() +
                            ") cannot be deleted via this endpoint. " +
                            "Please revoke access from your OAuth provider settings.");
        }

        if (password == null || password.isBlank()) {
            throw new BadRequestException("Password is required to delete account");
        }
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new BadRequestException("Incorrect password");
        }

        emailVerificationTokenRepository.deleteByUserId(userId);
        passwordResetTokenRepository.deleteByUserId(userId);
        userRepository.deleteById(userId);

        outboxMessageRepository.save(OutboxMessage.builder()
                .exchange(RabbitMqConfig.USER_DELETED_EXCHANGE)
                .routingKey("")
                .payload(serialize(new UserDeletedEvent(userId)))
                .payloadType(UserDeletedEvent.class.getName())
                .build());

        eventPublisher.publishEvent(new UserDeletedEvent(userId));
        log.info("Account deleted and UserDeletedEvent queued — userId={}", userId);
    }

    private String serialize(Object event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(
                    "Serialization failed for " + event.getClass().getSimpleName(), e);
        }
    }
}