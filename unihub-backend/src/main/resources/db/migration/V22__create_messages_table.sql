CREATE TABLE messages (
    id              UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    conversation_id UUID        NOT NULL,
    sender_id       UUID        NOT NULL,
    content         TEXT        NOT NULL,
    status          VARCHAR(10) NOT NULL DEFAULT 'SENT',
    created_at      TIMESTAMP            DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_message_conversation
        FOREIGN KEY (conversation_id) REFERENCES conversations(id) ON DELETE CASCADE,
    CONSTRAINT fk_message_sender
        FOREIGN KEY (sender_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_messages_conv_id   ON messages(conversation_id);
CREATE INDEX idx_messages_sender_id ON messages(sender_id);
