CREATE TABLE refresh_tokens (
    id                   UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    token_hash           VARCHAR(64) NOT NULL,          -- uniqueness via idx_rt_token_hash below
    user_id              UUID        NOT NULL,
    expires_at           TIMESTAMPTZ NOT NULL,
    revoked              BOOLEAN     NOT NULL DEFAULT FALSE,
    replaced_by_token_id UUID,
    created_at           TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_rt_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_rt_replaced_by
        FOREIGN KEY (replaced_by_token_id) REFERENCES refresh_tokens(id) ON DELETE SET NULL
);

-- Named unique index — matches @Index(name="idx_rt_token_hash", unique=true) in entity.
CREATE UNIQUE INDEX idx_rt_token_hash ON refresh_tokens(token_hash);

-- Named non-unique index — matches @Index(name="idx_rt_user_id") in entity.
CREATE INDEX idx_rt_user_id ON refresh_tokens(user_id);
