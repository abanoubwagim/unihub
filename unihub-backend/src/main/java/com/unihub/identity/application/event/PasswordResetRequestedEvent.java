package com.unihub.identity.application.event;

import java.util.UUID;

public record PasswordResetRequestedEvent(
        UUID userId,
        String email,
        String otp
) {}