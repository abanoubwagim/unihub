package com.unihub.identity.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import com.unihub.identity.domain.model.EmailVerificationToken;

public interface JpaEmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, UUID> {

    Optional<EmailVerificationToken> findByUserId(UUID userId);
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    void deleteByUserId(UUID userId);

}
