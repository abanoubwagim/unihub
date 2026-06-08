package com.unihub.company.api.dto.res;

import java.util.UUID;

public record CompanyProfileResponse(
        UUID id,
        UUID userId,
        String name,
        String description,
        String profilePhotoUrl,
        String websiteUrl,
        Integer countryId,
        String specialization,
        int hiredStudentCount
) {
}
