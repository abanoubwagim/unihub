package com.unihub.identity.api.dto;

import java.util.UUID;

import com.unihub.identity.domain.Role;
import com.unihub.identity.domain.UserStatus;

public record RegisterResponse(

    UUID userId,
    String email,
    Role role,
    UserStatus status
) {

}
