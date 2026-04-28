package com.unihub.identity.infrastructure;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.unihub.identity.domain.EmailVerificationToken;
import com.unihub.identity.domain.EmailVerificationTokenRepository;

import jakarta.transaction.Transactional;
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
