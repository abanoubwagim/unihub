package com.unihub.identity.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.unihub.identity.domain.model.PasswordResetToken;
import com.unihub.identity.domain.repository.PasswordResetTokenRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class PasswordResetTokenRepositoryImpl implements PasswordResetTokenRepository {

    private final JpaPasswordResetTokenRepository jpa;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Optional<PasswordResetToken> findByUserId(UUID userId) {
        return jpa.findByUserId(userId);
    }

    @Override
    @Transactional
    public void deleteByUserId(UUID userId) {
        jpa.deleteByUserId(userId);
    }

    @Override
    public PasswordResetToken save(PasswordResetToken token) {
        return jpa.save(token);
    }

    @Override
    public Optional<PasswordResetToken> findByResetTokenHash(String plainResetToken) {

        if(plainResetToken == null || plainResetToken.isBlank()) {
            return Optional.empty();
        }
        return jpa.findAllByResetTokenHashIsNotNull()
                .stream()
                .filter(t -> passwordEncoder.matches(plainResetToken, t.getResetTokenHash()))
                .findFirst();
    }

}
