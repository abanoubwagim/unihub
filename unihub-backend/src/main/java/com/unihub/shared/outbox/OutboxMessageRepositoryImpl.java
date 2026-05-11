package com.unihub.shared.outbox;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class OutboxMessageRepositoryImpl implements OutboxMessageRepository {

    private final JpaOutboxMessageRepository jpa;

    @Override
    public OutboxMessage save(OutboxMessage message) {
        return jpa.save(message);
    }

    @Override
    public List<OutboxMessage> findUnpublishedForUpdate(int maxAttempts, int batchSize) {
        return jpa.findUnpublishedForUpdate(maxAttempts, batchSize);
    }
}