package com.unihub.identity.api.dto;

public record LoginResponse(
        String accessToken,
        String tokenType,    // always "Bearer"
        long expiresIn     // seconds until the access token expires
) {

    public static LoginResponse from(AuthenticationResult result) {
        return new LoginResponse(result.accessToken(), "Bearer", result.expiresIn());
    }
}
