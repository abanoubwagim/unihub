package com.unihub.identity.infrastructure.persistence.impl;

import com.unihub.identity.domain.model.RefreshToken;
import com.unihub.identity.domain.repository.RefreshTokenRepository;
import com.unihub.identity.infrastructure.persistence.jpa.JpaRefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class RefreshTokenRepositoryImpl implements RefreshTokenRepository {

    private final JpaRefreshTokenRepository jpa;

    @Override
    public RefreshToken save(RefreshToken token) {
        return jpa.save(token);
    }

    @Override
    public Optional<RefreshToken> findByTokenHash(String tokenHash) {
        return jpa.findByTokenHash(tokenHash);
    }

    @Override
    @Transactional
    public void revokeAllByUserId(UUID userId) {
        jpa.revokeAllByUserId(userId);
    }

    @Override
    @Transactional
    public void deleteAllExpired() {
        jpa.deleteAllExpired();
    }
}
