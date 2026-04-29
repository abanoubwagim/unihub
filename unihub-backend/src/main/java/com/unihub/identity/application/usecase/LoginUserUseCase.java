package com.unihub.identity.application.usecase;

import com.unihub.identity.api.dto.LoginRequest;
import com.unihub.identity.api.dto.LoginResponse;

public interface LoginUserUseCase {

    LoginResponse login(LoginRequest request);
}
