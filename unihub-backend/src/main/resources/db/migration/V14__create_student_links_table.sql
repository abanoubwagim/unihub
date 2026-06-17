CREATE TABLE student_links (
    id         UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    student_id UUID         NOT NULL,
    link_type  VARCHAR(20),
    label      VARCHAR(255),
    url        VARCHAR(255) NOT NULL,

    CONSTRAINT fk_student_link_student
        FOREIGN KEY (student_id) REFERENCES student_profile(id) ON DELETE CASCADE
);
