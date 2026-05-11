package com.unihub.identity.application.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.unihub.identity.application.usecase.DeleteAccountUseCase;
import com.unihub.identity.domain.enums.AuthProvider;
import com.unihub.identity.domain.event.UserDeletedEvent;
import com.unihub.identity.domain.model.User;
import com.unihub.identity.domain.repository.EmailVerificationTokenRepository;
import com.unihub.identity.domain.repository.PasswordResetTokenRepository;
import com.unihub.identity.domain.repository.UserRepository;
import com.unihub.shared.config.RabbitMqConfig;
import com.unihub.shared.exception.BadRequestException;
import com.unihub.shared.exception.NotFoundException;
import com.unihub.shared.outbox.OutboxMessage;
import com.unihub.shared.outbox.OutboxMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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

    @Override
    @Transactional
    @PreAuthorize("isAuthenticated()")
    public void deleteAccount(UUID userId, String password) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (user.getAuthProvider() != AuthProvider.LOCAL) {
            throw new BadRequestException(
                    "OAuth accounts (" + user.getAuthProvider().name().toLowerCase() +
                            ") cannot be deleted via this endpoint. " +
                            "Please revoke access from your OAuth provider settings.");
        }

        // LOCAL accounts
        if (password == null || password.isBlank()) {
            throw new BadRequestException("Password is required to delete account");
        }
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new BadRequestException("Incorrect password");
        }

        emailVerificationTokenRepository.deleteByUserId(userId);
        passwordResetTokenRepository.deleteByUserId(userId);
        userRepository.deleteById(userId);

        try {
            UserDeletedEvent event = new UserDeletedEvent(userId);
            String payload = objectMapper.writeValueAsString(event);

            OutboxMessage outboxMessage = OutboxMessage.builder()
                    .id(UUID.randomUUID())
                    .exchange(RabbitMqConfig.USER_DELETED_EXCHANGE)
                    .routingKey("")   // FanoutExchange ignores routing key
                    .payload(payload)
                    .payloadType(UserDeletedEvent.class.getName())
                    .createdAt(LocalDateTime.now())
                    .attempts(0)
                    .build();

            outboxMessageRepository.save(outboxMessage);

        } catch (JsonProcessingException e) {
            throw new IllegalStateException(
                    "Failed to serialize UserDeletedEvent — userId=" + userId, e);
        }

        log.info("Account deleted and UserDeletedEvent queued in outbox — userId={}", userId);
    }
}