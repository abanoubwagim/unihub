CREATE TABLE password_reset_tokens (
    id                     UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id                UUID         NOT NULL UNIQUE,
    otp_hash               VARCHAR(255) NOT NULL,
    expires_at             TIMESTAMP    NOT NULL,
    created_at             TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    used                   BOOLEAN      NOT NULL DEFAULT FALSE,
    attempts               INTEGER      NOT NULL DEFAULT 0,
    reset_token            VARCHAR(255) UNIQUE,
    reset_token_expires_at TIMESTAMP,

    CONSTRAINT fk_prt_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
