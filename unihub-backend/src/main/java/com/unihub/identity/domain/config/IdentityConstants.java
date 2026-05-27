package com.unihub.identity.domain.config;

public final class IdentityConstants {

    private static final int TIME_UNIT_MINUTES = 5;

    public static final int RATE_LIMIT_MINUTES = TIME_UNIT_MINUTES;
    public static final int MAX_OTP_ATTEMPTS = TIME_UNIT_MINUTES;
    public static final int OTP_EXPIRY_MINUTES = TIME_UNIT_MINUTES;
    public static final int RESET_TOKEN_EXPIRY_MINUTES = TIME_UNIT_MINUTES;

    private IdentityConstants() {
    }
}