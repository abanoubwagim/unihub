CREATE TABLE outbox_messages
(
    id           UUID         NOT NULL PRIMARY KEY,
    exchange     VARCHAR(255) NOT NULL,
    routing_key  VARCHAR(255) NOT NULL,
    payload      TEXT         NOT NULL,
    payload_type VARCHAR(500) NOT NULL,
    created_at   TIMESTAMP    NOT NULL,
    published_at TIMESTAMP,
    attempts     INT          NOT NULL DEFAULT 0
);