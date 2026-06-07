package com.unihub.university.api.dto.res;

import java.util.List;
import java.util.UUID;

public record UniversityPublicResponse(
        UUID id,
        String name,
        String profilePhotoUrl,
        String websiteUrl,
        Integer countryId,
        int studentCount,
        List<MajorResponse> majors
) {
}
