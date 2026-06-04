package com.unihub.identity.api.dto.res;

import com.unihub.identity.domain.enums.Role;
import com.unihub.identity.domain.enums.UserStatus;

import java.util.UUID;

public record UserResponse(
        UUID id,
        String email,
        Role role,
        UserStatus status,
        boolean emailVerified
) {

}
