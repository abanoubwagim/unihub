package com.unihub.identity.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record VerifyEmailRequest(

        @Email
        @NotBlank(message = "Email is required")
        String email,

        @NotBlank
        @Size(min = 6, max = 6, message = "OTP must be 6 digits")
        String otp
) {

}
