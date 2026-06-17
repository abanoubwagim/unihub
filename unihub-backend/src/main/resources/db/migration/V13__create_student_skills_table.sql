CREATE TABLE student_skills (
    student_id UUID NOT NULL,
    skill_id   UUID NOT NULL,

    PRIMARY KEY (student_id, skill_id),

    CONSTRAINT fk_ss_student
        FOREIGN KEY (student_id) REFERENCES student_profile(id) ON DELETE CASCADE,
    CONSTRAINT fk_ss_skill
        FOREIGN KEY (skill_id) REFERENCES skills(id) ON DELETE CASCADE
);
