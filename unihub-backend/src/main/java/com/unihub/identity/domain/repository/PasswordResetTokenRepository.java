package com.unihub.identity.domain.repository;

import java.util.Optional;
import java.util.UUID;

import com.unihub.identity.domain.model.PasswordResetToken;


public interface PasswordResetTokenRepository {

    Optional<PasswordResetToken> findByUserId(UUID userId);
    
    Optional<PasswordResetToken> findByResetToken(String resetToken);
    
    PasswordResetToken save(PasswordResetToken token);
    
    void deleteByUserId(UUID userId);
}