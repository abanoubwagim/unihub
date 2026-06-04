package com.unihub.identity.application.usecase;

import com.unihub.identity.api.dto.res.UserResponse;

import java.util.UUID;

public interface GetCurrentUserUseCase {
    UserResponse getCurrentUser(UUID userId);
}
