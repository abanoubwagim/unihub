package com.unihub.identity.infrastructure.persistence.jpa;

import com.unihub.identity.domain.model.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import java.util.Optional;
import java.util.UUID;

public interface JpaPasswordResetTokenRepository extends JpaRepository<PasswordResetToken, UUID> {

    Optional<PasswordResetToken> findByUserId(UUID userId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    void deleteByUserId(UUID userId);

    Optional<PasswordResetToken> findByResetToken(String resetToken);
}
