package com.unihub.identity.application;

import java.util.UUID;

import com.unihub.identity.api.dto.UserResponse;

public interface GetCurrentUserUseCase {
    UserResponse getCurrentUser(UUID userId);
}
