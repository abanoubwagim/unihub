package com.unihub.identity.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequest(

    @NotBlank 
    String 
    resetToken,
    
    @NotBlank 
    @Size(min = 8, max = 72) 
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$", message = "Password must contain uppercase, lowercase, number, and symbol.")
    String newPassword,

    @NotBlank 
    String confirmPassword
) {

}
