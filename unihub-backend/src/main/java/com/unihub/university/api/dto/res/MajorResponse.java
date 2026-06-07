package com.unihub.university.api.dto.res;

import java.util.UUID;

public record MajorResponse(
        UUID id,
        String name
) {
}