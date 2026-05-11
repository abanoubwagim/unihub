package com.unihub.identity.api.dto;

import jakarta.validation.constraints.NotBlank;

public record ExchangeOAuth2CodeRequest(
    @NotBlank
    String code
) {}