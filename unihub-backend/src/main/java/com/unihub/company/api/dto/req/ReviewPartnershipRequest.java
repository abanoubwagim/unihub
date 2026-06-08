package com.unihub.company.api.dto.req;

import jakarta.validation.constraints.NotNull;

public record ReviewPartnershipRequest(

        @NotNull(message = "Accept flag is required")
        Boolean accept
) {
}