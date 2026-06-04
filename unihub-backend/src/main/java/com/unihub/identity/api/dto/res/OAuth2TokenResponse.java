package com.unihub.identity.api.dto.res;

public record OAuth2TokenResponse(
        String accessToken,
        String tokenType
) {
}