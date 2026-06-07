package com.unihub.student.domain.event;

import java.util.UUID;

public record StudentUniversitySetEvent(
        UUID studentProfileId,
        UUID userId,
        String name,
        String profilePhotoUrl,
        UUID universityId,
        UUID majorId,
        String level
) {
}