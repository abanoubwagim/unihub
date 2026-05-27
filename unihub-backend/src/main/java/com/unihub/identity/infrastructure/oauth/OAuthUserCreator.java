package com.unihub.identity.infrastructure.oauth;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.unihub.identity.domain.event.UserRegisteredEvent;
import com.unihub.identity.domain.model.User;
import com.unihub.identity.domain.repository.UserRepository;
import com.unihub.shared.config.RabbitMqConfig;
import com.unihub.shared.outbox.OutboxMessage;
import com.unihub.shared.outbox.OutboxMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;


@Slf4j
@Component
@RequiredArgsConstructor
class OAuthUserCreator {

    private final UserRepository userRepository;
    private final OutboxMessageRepository outboxMessageRepository;
    private final ObjectMapper objectMapper;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    User tryCreate(User newUser) {
        User saved = userRepository.save(newUser);
        writeRegistrationEventToOutbox(saved);
        log.info("Created new OAuth user — provider={}, role={}, userId={}",
                saved.getAuthProvider(), saved.getRole(), saved.getId());
        return saved;
    }


    private void writeRegistrationEventToOutbox(User user) {
        try {
            UserRegisteredEvent event = new UserRegisteredEvent(user.getId());
            String payload = objectMapper.writeValueAsString(event);

            OutboxMessage outboxMessage = OutboxMessage.builder()
                    .exchange(RabbitMqConfig.USER_REGISTERED_EXCHANGE)
                    .routingKey("")
                    .payload(payload)
                    .payloadType(UserRegisteredEvent.class.getName())
                    .createdAt(LocalDateTime.now())
                    .attempts(0)
                    .build();

            outboxMessageRepository.save(outboxMessage);

        } catch (JsonProcessingException e) {

            throw new IllegalStateException(
                    "Failed to serialize UserRegisteredEvent for outbox — userId=" + user.getId(), e);
        }
    }
}