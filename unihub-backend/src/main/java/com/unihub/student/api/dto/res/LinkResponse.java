package com.unihub.student.api.dto.res;

import com.unihub.student.domain.enums.LinkType;

import java.util.UUID;

public record LinkResponse(

        UUID id,
        LinkType type,
        String label,
        String url
) {
}