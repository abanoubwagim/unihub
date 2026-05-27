package com.unihub.identity.application.usecase;

import com.unihub.identity.api.dto.AuthenticationResult;

public interface TokenRotationUseCase {

    AuthenticationResult refresh(String rawRefreshToken);
}
