package com.unihub.identity.api.dto.req;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ResendVerificationRequest(

        @Email
        @NotBlank(message = "Email is required")
        String email
) {
}
