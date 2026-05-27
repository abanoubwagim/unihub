package com.unihub.student.api.dto.req;

import com.unihub.student.domain.enums.LinkType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record LinkRequest(

        @NotNull(message = "Link type is required")
        LinkType linkType,

        String label,

        @NotBlank(message = "URL is required")
        String url
) {
}