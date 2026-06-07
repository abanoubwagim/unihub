package com.unihub.university.api.dto.res;

import java.util.UUID;

public record UniversityStudentSummaryResponse(
        UUID profileId,
        UUID userId,
        String name,
        String profilePhotoUrl,
        UUID majorId,
        String level
) {
}