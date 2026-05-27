package com.unihub.identity.application.usecase;

import com.unihub.identity.api.dto.AuthenticationResult;
import com.unihub.identity.api.dto.LoginRequest;

public interface LoginUserUseCase {

    AuthenticationResult login(LoginRequest request);
}
