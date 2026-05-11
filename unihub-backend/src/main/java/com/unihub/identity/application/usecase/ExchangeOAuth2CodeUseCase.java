package com.unihub.identity.application.usecase;

import com.unihub.identity.api.dto.OAuth2TokenResponse;

public interface ExchangeOAuth2CodeUseCase {
    OAuth2TokenResponse exchange(String code);
}