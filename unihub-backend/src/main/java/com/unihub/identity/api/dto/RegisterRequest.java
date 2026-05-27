package com.unihub.identity.api.dto;

import com.unihub.identity.domain.enums.Role;
import jakarta.validation.constraints.*;

public record RegisterRequest(

        @Email
        @NotBlank(message = "Email is required")
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 72)
        @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&]).*$", message = "Password must contain uppercase, lowercase, number, and symbol.")
        String password,

        @NotBlank
        String confirmPassword,

        @NotNull
        Role role
) {

}
