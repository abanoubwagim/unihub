package com.unihub.identity.api.dto;

public record OAuth2TokenResponse(
        String accessToken,
        String tokenType
) {}