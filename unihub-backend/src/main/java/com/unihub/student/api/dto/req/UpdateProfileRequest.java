package com.unihub.student.api.dto.req;

import com.unihub.shared.validation.ValidCountryId;
import com.unihub.student.domain.enums.StudentLevel;
import jakarta.validation.constraints.NotNull;

public record UpdateProfileRequest(
        String name,
        String bio,
        StudentLevel level,

        @ValidCountryId
        @NotNull
        Integer countryId,

        String lookingFor,
        Integer graduationYear
) {
}