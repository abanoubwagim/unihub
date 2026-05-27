package com.unihub.identity.api.dto;

import jakarta.validation.constraints.NotBlank;

public record ExchangeOAuth2CodeRequest(

        @NotBlank(message = "Authorization code must not be empty")
        String code
) {
}