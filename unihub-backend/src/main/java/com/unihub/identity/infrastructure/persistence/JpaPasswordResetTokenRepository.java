package com.unihub.identity.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.unihub.identity.domain.model.PasswordResetToken;

public interface JpaPasswordResetTokenRepository extends JpaRepository<PasswordResetToken, UUID> {

    Optional<PasswordResetToken> findByUserId(UUID userId);

    @Modifying
    void deleteByUserId(UUID userId);

    @Query("SELECT t FROM PasswordResetToken t WHERE t.resetTokenHash IS NOT NULL")
    Optional<PasswordResetToken> findByResetToken(String resetToken);
}
