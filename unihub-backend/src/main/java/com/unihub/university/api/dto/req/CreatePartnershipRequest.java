package com.unihub.university.api.dto.req;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreatePartnershipRequest(

        @NotNull(message = "Company ID is required")
        UUID companyId
) {
}