package com.unihub.identity.infrastructure.persistence.jpa;

import com.unihub.identity.domain.model.EmailVerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import java.util.Optional;
import java.util.UUID;

public interface JpaEmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, UUID> {

    Optional<EmailVerificationToken> findByUserId(UUID userId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    void deleteByUserId(UUID userId);

}
