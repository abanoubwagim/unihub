package com.unihub.identity.application.event;

import com.unihub.identity.domain.enums.Role;

import java.util.UUID;

public record EmailVerifiedEvent(UUID userId, Role role) {
}