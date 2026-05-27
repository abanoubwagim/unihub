package com.unihub.identity.application.usecase;

import com.unihub.identity.domain.model.RefreshToken;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenUseCase {

    CreationResult create(UUID userId);

    Optional<RefreshToken> findByRawToken(String rawToken);

    void revoke(RefreshToken token);

    void revokeAllForUser(UUID userId);

    String extractBearerToken(HttpServletRequest request);

    record CreationResult(RefreshToken entity, String rawToken) {
    }
}
