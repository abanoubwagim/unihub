package com.unihub.shared.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseCookie;

import java.util.Arrays;
import java.util.Optional;

public final class CookieUtil {

    public static final String REFRESH_TOKEN_COOKIE = "refreshToken";
    private static final String AUTH_PATH = "/api/v1/auth";

    private CookieUtil() {
    }

    public static ResponseCookie buildRefreshCookie(String rawToken, long maxAgeSec) {
        return ResponseCookie.from(REFRESH_TOKEN_COOKIE, rawToken)
                .httpOnly(true)          // not readable by JavaScript
                .secure(true)            // HTTPS only
                .sameSite("Strict")      // blocks cross-site request forgery
                .path(AUTH_PATH)      // browser sends cookie only to /refresh
                .maxAge(maxAgeSec)
                .build();
    }

    public static ResponseCookie buildExpiredRefreshCookie() {
        return ResponseCookie.from(REFRESH_TOKEN_COOKIE, "")
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .path(AUTH_PATH)
                .maxAge(0)
                .build();
    }


    public static Optional<String> extractRefreshToken(HttpServletRequest request) {
        if (request.getCookies() == null) return Optional.empty();
        return Arrays.stream(request.getCookies())
                .filter(c -> REFRESH_TOKEN_COOKIE.equals(c.getName()))
                .map(Cookie::getValue)
                .filter(v -> v != null && !v.isBlank())
                .findFirst();
    }
}