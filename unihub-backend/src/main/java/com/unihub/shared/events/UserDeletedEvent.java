package com.unihub.shared.events;

import java.util.UUID;

public record UserDeletedEvent(
        UUID userId
) {
}