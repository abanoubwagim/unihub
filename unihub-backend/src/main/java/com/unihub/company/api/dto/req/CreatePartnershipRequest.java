package com.unihub.company.api.dto.req;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreatePartnershipRequest(

        @NotNull(message = "University ID is required")
        UUID universityId
) {
}