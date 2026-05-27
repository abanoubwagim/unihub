package com.unihub.student.api.dto.res;

import com.unihub.student.domain.enums.AcademicStatus;
import com.unihub.student.domain.enums.StudentLevel;

import java.util.List;
import java.util.UUID;

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
        List<String> skills,
        List<LinkResponse> links
) {
}