package com.unihub.identity.api.dto.req;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record VerifyResetOtpRequest(

        @Email
        @NotBlank(message = "Email is required")
        String email,

        @NotBlank(message = "Otp is reuired")
        @Size(min = 6, max = 6, message = "Otp must be 6 digits")
        String otp
) {

}
