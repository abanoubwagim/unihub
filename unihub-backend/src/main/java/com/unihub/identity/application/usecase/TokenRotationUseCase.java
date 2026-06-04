package com.unihub.identity.application.usecase;

import com.unihub.identity.api.dto.res.AuthenticationResult;

public interface TokenRotationUseCase {

    AuthenticationResult refresh(String rawRefreshToken);
}
