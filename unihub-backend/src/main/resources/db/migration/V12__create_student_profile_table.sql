CREATE TABLE student_profile (
    id                UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id           UUID        NOT NULL UNIQUE,
    name              VARCHAR(255),
    bio               TEXT,
    profile_photo_url VARCHAR(255),
    academic_status   VARCHAR(20) NOT NULL DEFAULT 'UNDERGRADUATE',
    level             VARCHAR(10),
    university_id     UUID,
    major_id          UUID,
    country_id        INTEGER,
    looking_for       TEXT,
    graduation_year   INTEGER,
    is_verified       BOOLEAN     NOT NULL DEFAULT FALSE,
    is_locked         BOOLEAN     NOT NULL DEFAULT FALSE,
    created_at        TIMESTAMP            DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP,

    CONSTRAINT fk_student_profile_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_student_profile_country
        FOREIGN KEY (country_id) REFERENCES countries(id),
    CONSTRAINT fk_student_profile_university
        FOREIGN KEY (university_id) REFERENCES university_profiles(id) ON DELETE SET NULL,
    CONSTRAINT fk_student_profile_major
        FOREIGN KEY (major_id) REFERENCES majors(id) ON DELETE SET NULL
);
