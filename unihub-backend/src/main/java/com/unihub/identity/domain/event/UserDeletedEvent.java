package com.unihub.identity.domain.event;

import java.util.UUID;

public record UserDeletedEvent(
    UUID userId
) {
}