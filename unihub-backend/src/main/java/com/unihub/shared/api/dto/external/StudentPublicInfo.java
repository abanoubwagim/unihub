package com.unihub.shared.api.dto.external;

import java.util.UUID;

public record StudentPublicInfo(

        UUID userId,
        UUID profileId,
        String name,
        String profilePhotoUrl,
        UUID majorId,
        UUID universityId,
        String level
) {
}