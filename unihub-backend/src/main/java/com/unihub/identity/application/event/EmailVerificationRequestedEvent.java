package com.unihub.identity.application.event;

import java.util.UUID;

public record EmailVerificationRequestedEvent(
        UUID userId,
        String email,
        String otp) {

}
