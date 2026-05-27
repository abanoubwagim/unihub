package com.unihub.identity.application.impl;

import com.unihub.identity.application.usecase.RefreshTokenUseCase;
import com.unihub.identity.domain.model.RefreshToken;
import com.unihub.identity.domain.repository.RefreshTokenRepository;
import com.unihub.shared.util.TokenHashUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenUseCaseImpl implements RefreshTokenUseCase {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${jwt.refresh-expiration-seconds}")
    private long refreshExpirationSeconds;

    @Override
    @Transactional
    public CreationResult create(UUID userId) {
        String rawToken = generateRawToken();     // 256-bit random — only returned once
        String hash = TokenHashUtil.sha256(rawToken);

        RefreshToken entity = RefreshToken.builder()
                .tokenHash(hash)
                .userId(userId)
                .expiresAt(Instant.now().plusSeconds(refreshExpirationSeconds))
                .revoked(false)
                .build();

        refreshTokenRepository.save(entity);
        log.debug("Refresh token created — userId={}", userId);
        return new CreationResult(entity, rawToken);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<RefreshToken> findByRawToken(String rawToken) {
        String hash = TokenHashUtil.sha256(rawToken);
        return refreshTokenRepository.findByTokenHash(hash);
    }

    @Override
    @Transactional
    public void revoke(RefreshToken token) {
        token.setRevoked(true);
        refreshTokenRepository.save(token);
        log.debug("Refresh token revoked — id={}, userId={}", token.getId(), token.getUserId());
    }

    @Override
    @Transactional
    public void revokeAllForUser(UUID userId) {
        refreshTokenRepository.revokeAllByUserId(userId);
        log.info("All refresh tokens revoked — userId={}", userId);
    }

    @Override
    public String extractBearerToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

    private String generateRawToken() {
        byte[] bytes = new byte[32];           // 256 bits of entropy
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
