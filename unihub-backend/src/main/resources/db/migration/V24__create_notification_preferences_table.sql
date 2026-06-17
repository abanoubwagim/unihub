CREATE TABLE notification_preferences
(
    id                UUID PRIMARY KEY     DEFAULT gen_random_uuid(),
    user_id           UUID        NOT NULL,
    notification_type VARCHAR(60) NOT NULL,
    in_app_enabled    BOOLEAN     NOT NULL DEFAULT TRUE,

    CONSTRAINT uq_notif_prefs_user_type
        UNIQUE (user_id, notification_type),
    CONSTRAINT fk_notif_pref_user
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);
