package com.unihub.identity.application.usecase;

import java.util.UUID;

import com.unihub.identity.api.dto.UserResponse;

public interface GetCurrentUserUseCase {
    UserResponse getCurrentUser(UUID userId);
}
