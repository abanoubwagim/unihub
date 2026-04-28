package com.unihub.identity.application;

import com.unihub.identity.api.dto.RegisterReqeust;
import com.unihub.identity.api.dto.RegisterResponse;

public interface RegisterUserUseCase {
    RegisterResponse register(RegisterReqeust request);
}
