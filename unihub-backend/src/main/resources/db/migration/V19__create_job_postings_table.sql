CREATE TABLE job_postings (
    id                 UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    company_id         UUID        NOT NULL,
    title              VARCHAR(200),
    job_type           VARCHAR(20),
    work_location_type VARCHAR(20),
    salary_from        INTEGER,
    salary_to          INTEGER,
    description        TEXT,
    deadline           DATE,
    status             VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    applicant_count    INTEGER     NOT NULL DEFAULT 0,
    published_at       TIMESTAMP,
    created_at         TIMESTAMP            DEFAULT CURRENT_TIMESTAMP,
    updated_at         TIMESTAMP,

    CONSTRAINT fk_job_posting_company
        FOREIGN KEY (company_id) REFERENCES company_profiles(id) ON DELETE CASCADE
);
