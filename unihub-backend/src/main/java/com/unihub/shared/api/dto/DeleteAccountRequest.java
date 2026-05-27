package com.unihub.shared.api.dto;

import jakarta.validation.constraints.NotBlank;

public record DeleteAccountRequest(

        @NotBlank(message = "Password must not be Empty")
        String password
) {

}
