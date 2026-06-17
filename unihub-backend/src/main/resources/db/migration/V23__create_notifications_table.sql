CREATE TABLE notifications (
    id             UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id        UUID         NOT NULL,
    type           VARCHAR(60)  NOT NULL,
    title          VARCHAR(200) NOT NULL,
    body           TEXT         NOT NULL,
    reference_id   UUID,
    reference_type VARCHAR(50),
    is_read        BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    read_at        TIMESTAMP,

    CONSTRAINT fk_notification_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_notifications_user_id
    ON notifications(user_id);

CREATE INDEX idx_notifications_user_id_is_read
    ON notifications(user_id, is_read);
