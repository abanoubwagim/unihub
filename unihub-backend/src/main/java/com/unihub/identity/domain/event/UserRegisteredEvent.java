package com.unihub.identity.domain.event;

import java.util.UUID;
import com.unihub.identity.domain.enums.Role;

public record UserRegisteredEvent(
    UUID userId,
    Role role
) {}