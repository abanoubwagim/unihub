package com.unihub.identity.application.usecase;

import com.unihub.identity.api.dto.req.LoginRequest;
import com.unihub.identity.api.dto.res.AuthenticationResult;

public interface LoginUserUseCase {

    AuthenticationResult login(LoginRequest request);
}
