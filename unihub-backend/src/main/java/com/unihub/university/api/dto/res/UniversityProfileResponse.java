package com.unihub.university.api.dto.res;

import java.util.List;
import java.util.UUID;

public record UniversityProfileResponse(
        UUID id,
        UUID userId,
        String name,
        String bio,
        String profilePhotoUrl,
        String websiteUrl,
        String address,
        Integer countryId,
        int studentCount,
        int graduateCount,
        List<MajorResponse> majors
) {
}