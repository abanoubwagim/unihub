CREATE TABLE student_certifications (
    id                   UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    student_id           UUID         NOT NULL,
    title                VARCHAR(255) NOT NULL,
    issuing_organization VARCHAR(255) NOT NULL,
    date_issued          DATE         NOT NULL,
    file_url             VARCHAR(255),

    CONSTRAINT fk_student_cert_student
        FOREIGN KEY (student_id) REFERENCES student_profile(id) ON DELETE CASCADE
);
