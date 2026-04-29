package com.unihub.identity.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.unihub.identity.domain.model.EmailVerificationToken;
import com.unihub.identity.domain.repository.EmailVerificationTokenRepository;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class EmailVerificationTokenRepositoryImpl implements EmailVerificationTokenRepository {

    private final JpaEmailVerificationTokenRepository jpa;

    @Override
    public Optional<EmailVerificationToken> findByUserId(UUID userId) {
        return jpa.findByUserId(userId);
    }

    @Override
    public EmailVerificationToken save(EmailVerificationToken token) {
        return jpa.save(token);
    }

    @Override
    @Transactional
    public void deleteByUserId(UUID userId) {
        jpa.deleteByUserId(userId);
    }

}
