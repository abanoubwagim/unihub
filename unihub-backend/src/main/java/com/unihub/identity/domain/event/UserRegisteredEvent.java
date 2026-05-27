package com.unihub.identity.domain.event;

import java.util.UUID;

public record UserRegisteredEvent(
        UUID userId
) {
}