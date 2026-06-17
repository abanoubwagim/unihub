CREATE TABLE job_applications (
    id                 UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    job_posting_id     UUID         NOT NULL,
    student_profile_id UUID         NOT NULL,
    cv_url             VARCHAR(500) NOT NULL,
    status             VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    rejection_reason   TEXT,
    submitted_at       TIMESTAMP             DEFAULT CURRENT_TIMESTAMP,
    reviewed_at        TIMESTAMP,

    CONSTRAINT uq_job_application
        UNIQUE (job_posting_id, student_profile_id),
    CONSTRAINT fk_job_app_posting
        FOREIGN KEY (job_posting_id) REFERENCES job_postings(id) ON DELETE CASCADE,
    CONSTRAINT fk_job_app_student
        FOREIGN KEY (student_profile_id) REFERENCES student_profile(id) ON DELETE CASCADE
);
