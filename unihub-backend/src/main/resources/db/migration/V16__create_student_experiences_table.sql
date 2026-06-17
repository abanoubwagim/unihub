CREATE TABLE student_experiences (
    id          UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    student_id  UUID         NOT NULL,
    job_title   VARCHAR(255) NOT NULL,
    company     VARCHAR(255) NOT NULL,
    start_date  DATE         NOT NULL,
    end_date    DATE,
    is_current  BOOLEAN               DEFAULT FALSE,
    job_type    VARCHAR(20)  NOT NULL,
    location    VARCHAR(255),
    description TEXT,

    CONSTRAINT fk_student_exp_student
        FOREIGN KEY (student_id) REFERENCES student_profile(id) ON DELETE CASCADE
);

CREATE TABLE experience_skills (
    experience_id UUID NOT NULL,
    skill_id      UUID NOT NULL,

    PRIMARY KEY (experience_id, skill_id),

    CONSTRAINT fk_es_experience
        FOREIGN KEY (experience_id) REFERENCES student_experiences(id) ON DELETE CASCADE,
    CONSTRAINT fk_es_skill
        FOREIGN KEY (skill_id) REFERENCES skills(id) ON DELETE CASCADE
);
