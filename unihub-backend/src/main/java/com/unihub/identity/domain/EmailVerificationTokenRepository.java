package com.unihub.identity.domain;

import java.util.Optional;
import java.util.UUID;

public interface EmailVerificationTokenRepository {

    Optional<EmailVerificationToken> findByUserId(UUID userId);
    EmailVerificationToken save(EmailVerificationToken token);
    void deleteByUserId(UUID userId);

}
