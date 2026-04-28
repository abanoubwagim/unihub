package com.unihub.identity.api.dto;

import com.unihub.identity.domain.Role;
import com.unihub.identity.domain.UserStatus;
import java.util.UUID;

public record UserResponse(
    UUID id,
    String email,
    Role role,
    UserStatus status,
    boolean emailVerified
) {

}
