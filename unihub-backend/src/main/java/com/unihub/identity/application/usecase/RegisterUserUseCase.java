package com.unihub.identity.application.usecase;

import com.unihub.identity.api.dto.req.RegisterRequest;
import com.unihub.identity.api.dto.res.RegisterResponse;

public interface RegisterUserUseCase {
    RegisterResponse register(RegisterRequest request);
}
