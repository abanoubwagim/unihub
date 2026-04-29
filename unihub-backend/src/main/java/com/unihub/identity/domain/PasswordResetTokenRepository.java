package com.unihub.identity.domain;

import java.util.Optional;
import java.util.UUID;


public interface PasswordResetTokenRepository {

    Optional<PasswordResetToken> findByUserId(UUID userId);
    
    Optional<PasswordResetToken> findByResetToken(String resetToken);
    
    PasswordResetToken save(PasswordResetToken token);
    
    void deleteByUserId(UUID userId);
}