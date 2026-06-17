CREATE TABLE university_majors
(
    university_id UUID NOT NULL,
    major_id      UUID NOT NULL,

    PRIMARY KEY (university_id, major_id),

    CONSTRAINT fk_um_university
        FOREIGN KEY (university_id) REFERENCES university_profiles (id) ON DELETE CASCADE,
    CONSTRAINT fk_um_major
        FOREIGN KEY (major_id) REFERENCES majors (id) ON DELETE CASCADE
);
