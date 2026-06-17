CREATE TABLE company_profiles (
    id                  UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id             UUID         NOT NULL UNIQUE,
    name                VARCHAR(200),
    description         TEXT,
    website_url         VARCHAR(500),
    country_id          INTEGER,
    specialization      VARCHAR(100),
    profile_photo_url   VARCHAR(500),
    hired_student_count INTEGER      NOT NULL DEFAULT 0,
    created_at          TIMESTAMP             DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP,

    CONSTRAINT fk_company_profile_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_company_profile_country
        FOREIGN KEY (country_id) REFERENCES countries(id)
);
