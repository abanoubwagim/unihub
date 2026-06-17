CREATE TABLE university_profiles (
    id                UUID    PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id           UUID    NOT NULL UNIQUE,
    name              VARCHAR(255),
    bio               TEXT,
    profile_photo_url VARCHAR(255),
    website_url       VARCHAR(255),
    address           VARCHAR(255),
    country_id        INTEGER,
    student_count     INTEGER NOT NULL DEFAULT 0,
    graduate_count    INTEGER NOT NULL DEFAULT 0,
    created_at        TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP,

    CONSTRAINT fk_univ_profile_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_univ_profile_country
        FOREIGN KEY (country_id) REFERENCES countries(id)
);
