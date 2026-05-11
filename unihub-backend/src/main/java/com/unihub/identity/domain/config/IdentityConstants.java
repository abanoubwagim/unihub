package com.unihub.identity.domain.config;

public final class IdentityConstants {

    private IdentityConstants() {}

    public static final int OTP_EXPIRY_MINUTES          = 5;
    public static final int RESET_TOKEN_EXPIRY_MINUTES  = 5;
    public static final int RATE_LIMIT_MINUTES          = 1;
    public static final int MAX_OTP_ATTEMPTS            = 5;
}