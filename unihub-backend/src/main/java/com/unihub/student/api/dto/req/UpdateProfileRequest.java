package com.unihub.student.api.dto.req;

import com.unihub.student.domain.enums.StudentLevel;

public record UpdateProfileRequest(
        String name,
        String bio,
        StudentLevel level,
        Integer countryId,
        String lookingFor,
        Integer graduationYear
) {
}