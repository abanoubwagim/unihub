package com.unihub.university.api.dto.req;

import jakarta.validation.constraints.NotNull;

public record ReviewCertificateRequest(

        @NotNull(message = "Approved flag is required")
        Boolean approved,

        String rejectionReason
) {
}