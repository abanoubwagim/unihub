package com.unihub.identity.domain.model;

import com.unihub.identity.domain.config.IdentityConstants;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "password_reset_tokens")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false, unique = true)
    private UUID userId;

    @Column(name = "otp_hash", nullable = false)
    private String otpHash;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "used", nullable = false)
    private boolean used;

    @Column(name = "attempts", nullable = false)
    private int attempts;

    @Column(name = "reset_token", unique = true)
    private String resetToken;

    @Column(name = "reset_token_expires_at")
    private LocalDateTime resetTokenExpiresAt;


    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    public void markUsed() {
        this.used = true;
    }

    public void incrementAttempts() {
        this.attempts++;
    }

    public void setResetToken(String hashedToken) {
        this.resetToken = hashedToken;
        this.resetTokenExpiresAt = LocalDateTime.now().plusMinutes(IdentityConstants.RESET_TOKEN_EXPIRY_MINUTES);
    }

    public boolean isResetTokenExpired() {
        return resetTokenExpiresAt == null || LocalDateTime.now().isAfter(resetTokenExpiresAt);
    }

    public void resetFor(String newOtpHash) {
        this.otpHash = newOtpHash;
        this.resetToken = null;
        this.resetTokenExpiresAt = null;
        this.used = false;
        this.attempts = 0;
        this.createdAt = LocalDateTime.now();
        this.expiresAt = LocalDateTime.now().plusMinutes(IdentityConstants.OTP_EXPIRY_MINUTES);
    }

}
