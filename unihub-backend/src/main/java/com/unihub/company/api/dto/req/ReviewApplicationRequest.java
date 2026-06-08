package com.unihub.company.api.dto.req;

import jakarta.validation.constraints.NotNull;

public record ReviewApplicationRequest(

        @NotNull(message = "Accepted flag is required")
        Boolean accepted,

        String rejectionReason
) {
}