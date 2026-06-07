package com.unihub.university.api.dto.req;

import com.unihub.shared.validation.ValidCountryId;
import jakarta.validation.constraints.NotNull;

public record UpdateProfileRequest(
        String name,
        String bio,
        String websiteUrl,
        String address,

        @ValidCountryId
        @NotNull
        Integer countryId
) {
}