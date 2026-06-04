package com.unihub.identity.api.dto.req;

import jakarta.validation.constraints.NotBlank;

public record ExchangeOAuth2CodeRequest(

        @NotBlank(message = "Authorization code must not be empty")
        String code
) {
}