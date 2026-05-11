package com.unihub.shared.outbox;

import java.util.List;

public interface OutboxMessageRepository {

    OutboxMessage save(OutboxMessage message);

    List<OutboxMessage> findUnpublishedForUpdate(int maxAttempts, int batchSize);
}