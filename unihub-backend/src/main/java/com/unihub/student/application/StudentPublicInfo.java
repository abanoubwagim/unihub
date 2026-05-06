package com.unihub.student.application;

import java.util.UUID;

public record StudentPublicInfo(

    UUID userId,
    UUID profileId,
    String name,
    String profilePhotoUrl,
    UUID majorId
) {
}