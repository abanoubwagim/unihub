CREATE TABLE student_projects (
    id           UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    student_id   UUID         NOT NULL,
    title        VARCHAR(255) NOT NULL,
    description  TEXT,
    is_current   BOOLEAN               DEFAULT FALSE,
    start_date   DATE,
    end_date     DATE,
    project_link VARCHAR(255),

    CONSTRAINT fk_student_proj_student
        FOREIGN KEY (student_id) REFERENCES student_profile(id) ON DELETE CASCADE
);

CREATE TABLE project_skills (
    project_id UUID NOT NULL,
    skill_id   UUID NOT NULL,

    PRIMARY KEY (project_id, skill_id),

    CONSTRAINT fk_ps_project
        FOREIGN KEY (project_id) REFERENCES student_projects(id) ON DELETE CASCADE,
    CONSTRAINT fk_ps_skill
        FOREIGN KEY (skill_id) REFERENCES skills(id) ON DELETE CASCADE
);
