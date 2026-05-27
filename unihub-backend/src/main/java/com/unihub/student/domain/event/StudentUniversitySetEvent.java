package com.unihub.student.domain.event;

import java.util.UUID;

public record StudentUniversitySetEvent(
        UUID studentProfileId,
        UUID universityId
) {
}