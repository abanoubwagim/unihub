package com.unihub.shared.api.dto.external;

import java.util.UUID;

public record UniversityPublicInfo(
        UUID userId,
        UUID profileId,
        String name,
        String profilePhotoUrl,
        Integer countryId,
        int studentCount,
        int graduateCount
) {
}