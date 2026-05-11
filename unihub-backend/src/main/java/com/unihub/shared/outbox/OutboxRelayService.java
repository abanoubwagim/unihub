package com.unihub.shared.outbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OutboxRelayService {

    private final OutboxMessageRepository outboxMessageRepository;
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;
    private final TransactionTemplate transactionTemplate;

    private static final int MAX_ATTEMPTS = 3;
    private static final int BATCH_SIZE   = 50;

    @Scheduled(fixedDelayString = "${outbox.relay.fixed-delay-ms:5000}")
    public void relay() {
        
        for (int i = 0; i < BATCH_SIZE; i++) {
            Boolean hadWork = transactionTemplate.execute(status -> {
                List<OutboxMessage> locked =
                        outboxMessageRepository.findUnpublishedForUpdate(MAX_ATTEMPTS, 1);

                if (locked.isEmpty()) {
                    return false;
                }

                OutboxMessage message = locked.get(0);

                try {
                    Class<?> payloadClass = Class.forName(message.getPayloadType());
                    Object event = objectMapper.readValue(message.getPayload(), payloadClass);

                    rabbitTemplate.convertAndSend(
                            message.getExchange(),
                            message.getRoutingKey(),
                            event);

                    message.markPublished();
                    outboxMessageRepository.save(message);

                    log.debug("Outbox relay: published {} — id={}",
                            message.getPayloadType(), message.getId());

                } catch (Exception e) {
                    message.incrementAttempts();
                    outboxMessageRepository.save(message);

                    log.error("Outbox relay: failed to publish — id={}, type={}, attempts={}, error={}",
                            message.getId(), message.getPayloadType(),
                            message.getAttempts(), e.getMessage(), e);

                    if (message.getAttempts() >= MAX_ATTEMPTS) {
                        log.error("Outbox relay: GIVING UP after {} attempts — id={}, type={}. "
                                + "Manual intervention required.",
                                MAX_ATTEMPTS, message.getId(), message.getPayloadType());
                    }
                }

                return true;
            });

            if (!Boolean.TRUE.equals(hadWork)) {
                break;
            }
        }
    }
}