package com.unihub.identity.application.impl;

import com.unihub.identity.api.dto.res.AuthenticationResult;
import com.unihub.identity.application.usecase.RefreshTokenUseCase;
import com.unihub.identity.application.usecase.TokenRotationUseCase;
import com.unihub.identity.domain.model.RefreshToken;
import com.unihub.identity.domain.model.User;
import com.unihub.identity.domain.repository.UserRepository;
import com.unihub.shared.exception.InvalidTokenException;
import com.unihub.shared.exception.SecurityViolationException;
import com.unihub.shared.security.JwtSubject;
import com.unihub.shared.security.service.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenRotationUseCaseImpl implements TokenRotationUseCase {

    private final RefreshTokenUseCase refreshTokenUseCase;
    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public AuthenticationResult refresh(String rawRefreshToken) {

        RefreshToken stored = refreshTokenUseCase.findByRawToken(rawRefreshToken)
                .orElseThrow(() -> new InvalidTokenException("Refresh token not found"));

        if (stored.isRevoked()) {
            log.warn("SECURITY — Refresh token reuse detected. Revoking all sessions. userId={}",
                    stored.getUserId());
            refreshTokenUseCase.revokeAllForUser(stored.getUserId());
            throw new SecurityViolationException(
                    "Security violation: token reuse detected. All sessions have been terminated. " +
                            "Please log in again.");
        }

        if (stored.getExpiresAt().isBefore(Instant.now())) {
            throw new InvalidTokenException("Refresh token has expired. Please log in again.");
        }

        UUID userId = stored.getUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new InvalidTokenException("Associated user no longer exists"));

        refreshTokenUseCase.revoke(stored);
        RefreshTokenUseCase.CreationResult newRefresh = refreshTokenUseCase.create(userId);

        stored.setReplacedByTokenId(newRefresh.entity().getId());

        String newAccessToken = jwtService.generateToken(
                new JwtSubject(user.getId(), user.getEmail(), user.getRole().name()));

        long expiresIn = jwtService.getExpirationSeconds(newAccessToken);

        log.debug("Token rotated — userId={}", userId);

        return new AuthenticationResult(newAccessToken, newRefresh.rawToken(), expiresIn);

    }
}
