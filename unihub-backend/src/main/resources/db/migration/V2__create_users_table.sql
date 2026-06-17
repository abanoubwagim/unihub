CREATE TABLE users (
    id                  UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    email               VARCHAR(255) NOT NULL UNIQUE,
    role                VARCHAR(20)  NOT NULL,
    password_hash       VARCHAR(255),
    status              VARCHAR(20)  NOT NULL,
    auth_provider       VARCHAR(20)  NOT NULL,
    email_verified      BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at          TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP,
    password_changed_at TIMESTAMP
);
