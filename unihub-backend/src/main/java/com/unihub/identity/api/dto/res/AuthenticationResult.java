package com.unihub.identity.api.dto.res;


public record AuthenticationResult(
        String accessToken,
        String rawRefreshToken,
        long expiresIn          // seconds until access token expires
) {
}