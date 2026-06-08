package com.unihub.company.api.dto.req;

import com.unihub.shared.validation.ValidCountryId;
import jakarta.validation.constraints.NotNull;

public record UpdateProfileRequest(

        String name,
        String description,
        String websiteUrl,

        @ValidCountryId
        @NotNull
        Integer countryId,

        String specialization
) {
}