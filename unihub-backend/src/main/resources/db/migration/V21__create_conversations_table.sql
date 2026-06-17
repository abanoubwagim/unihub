CREATE TABLE conversations (
    id              UUID      PRIMARY KEY DEFAULT gen_random_uuid(),
    participant1_id UUID      NOT NULL,
    participant2_id UUID      NOT NULL,
    created_at      TIMESTAMP          DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP,

    CONSTRAINT uq_conversation_participants
        UNIQUE (participant1_id, participant2_id),
    CONSTRAINT fk_conv_participant1
        FOREIGN KEY (participant1_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_conv_participant2
        FOREIGN KEY (participant2_id) REFERENCES users(id) ON DELETE CASCADE
);
