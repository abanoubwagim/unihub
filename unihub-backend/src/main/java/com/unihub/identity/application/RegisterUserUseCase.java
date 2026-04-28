package com.unihub.identity.application;

import com.unihub.identity.api.dto.RegisterRequest;
import com.unihub.identity.api.dto.RegisterResponse;

public interface RegisterUserUseCase {
    RegisterResponse register(RegisterRequest request);
}
