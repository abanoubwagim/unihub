package com.unihub.identity.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.unihub.identity.domain.model.EmailVerificationToken;

public interface JpaEmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, UUID> {

    Optional<EmailVerificationToken> findByUserId(UUID userId);
    void deleteByUserId(UUID userId);

}
