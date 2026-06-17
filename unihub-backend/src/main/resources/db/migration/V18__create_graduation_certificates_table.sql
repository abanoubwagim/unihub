CREATE TABLE graduation_certificates (
    id               UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    student_id       UUID        NOT NULL,
    university_id    UUID        NOT NULL,
    file_url         VARCHAR(255),
    status           VARCHAR(25) NOT NULL DEFAULT 'PENDING',
    rejection_reason TEXT,
    attempt_number   INTEGER     NOT NULL DEFAULT 0,
    submitted_at     TIMESTAMP            DEFAULT CURRENT_TIMESTAMP,
    reviewed_at      TIMESTAMP,

    CONSTRAINT fk_grad_cert_student
        FOREIGN KEY (student_id) REFERENCES student_profile(id) ON DELETE CASCADE,
    CONSTRAINT fk_grad_cert_university
        FOREIGN KEY (university_id) REFERENCES university_profiles(id)
);
