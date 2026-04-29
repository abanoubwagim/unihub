package com.unihub.identity.domain.repository;

import java.util.Optional;
import java.util.UUID;

import com.unihub.identity.domain.model.EmailVerificationToken;

public interface EmailVerificationTokenRepository {

    Optional<EmailVerificationToken> findByUserId(UUID userId);
    EmailVerificationToken save(EmailVerificationToken token);
    void deleteByUserId(UUID userId);

}
