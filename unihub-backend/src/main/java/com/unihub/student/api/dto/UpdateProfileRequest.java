package com.unihub.student.api.dto;

import com.unihub.student.domain.enums.AcademicStatus;
import com.unihub.student.domain.enums.StudentLevel;

public record UpdateProfileRequest(
    String name,
    String bio,
    AcademicStatus academicStatus,
    StudentLevel level,
    Integer countryId,
    String lookingFor,
    Integer graduationYear
) { }