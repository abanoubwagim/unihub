package com.unihub.identity.application.impl;

import com.unihub.identity.application.usecase.LogoutUseCase;
import com.unihub.identity.application.usecase.RefreshTokenUseCase;
import com.unihub.shared.security.service.JwtService;
import com.unihub.shared.security.service.TokenBlacklistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class LogoutUseCaseImpl implements LogoutUseCase {

    private final JwtService jwtService;
    private final TokenBlacklistService tokenBlacklistService;
    private final RefreshTokenUseCase refreshTokenUseCase;


    @Override
    public void logout(String rawAccessToken) {
        logout(rawAccessToken, null);
    }

    @Override
    public void logout(String rawAccessToken, String rawRefreshToken) {
        blacklistAccessToken(rawAccessToken);
        revokeRefreshToken(rawRefreshToken);
    }

    private void blacklistAccessToken(String rawAccessToken) {
        if (rawAccessToken == null || rawAccessToken.isBlank()) return;
        try {
            long ttl = jwtService.getExpirationSeconds(rawAccessToken);
            if (ttl > 0) {
                tokenBlacklistService.blacklist(rawAccessToken, ttl);
            }
        } catch (Exception e) {
            log.warn("Best-effort: failed to blacklist access token during logout — {}", e.getMessage());
        }
    }

    private void revokeRefreshToken(String rawRefreshToken) {
        if (rawRefreshToken == null || rawRefreshToken.isBlank()) return;
        try {
            refreshTokenUseCase.findByRawToken(rawRefreshToken)
                    .ifPresentOrElse(
                            refreshTokenUseCase::revoke,
                            () -> log.debug("Logout: refresh token not found in DB (may have already expired)")
                    );
        } catch (Exception e) {
            log.warn("Best-effort: failed to revoke refresh token during logout — {}", e.getMessage());
        }
    }
}