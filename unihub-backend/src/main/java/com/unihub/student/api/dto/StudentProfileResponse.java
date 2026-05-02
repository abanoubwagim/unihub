package com.unihub.student.api.dto;

import java.util.List;
import java.util.UUID;

import com.unihub.student.domain.enums.AcademicStatus;
import com.unihub.student.domain.enums.StudentLevel;

public record StudentProfileResponse(
    UUID id,
    UUID userId,
    String name,
    String bio,
    String profilePhotoUrl,
    AcademicStatus academicStatus,
    StudentLevel level,
    UUID universityId,
    UUID majorId,
    Integer countryId,
    String lookingFor,
    Integer graduationYear,
    boolean verified,
    List<String> skills,
    List<LinkResponse> links
) {}