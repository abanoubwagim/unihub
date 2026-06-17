CREATE TABLE university_partnerships (
    id            UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    university_id UUID        NOT NULL,
    company_id    UUID        NOT NULL,
    status        VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    requested_by  VARCHAR(20) NOT NULL,
    created_at    TIMESTAMP            DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP,

    CONSTRAINT uq_university_partnership
        UNIQUE (university_id, company_id),
    CONSTRAINT fk_partnership_university
        FOREIGN KEY (university_id) REFERENCES university_profiles(id) ON DELETE CASCADE,
    CONSTRAINT fk_partnership_company
        FOREIGN KEY (company_id) REFERENCES company_profiles(id) ON DELETE CASCADE
);
