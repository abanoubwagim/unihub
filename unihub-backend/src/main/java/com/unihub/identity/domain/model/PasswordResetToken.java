package com.unihub.identity.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "password_reset_tokens")
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class PasswordResetToken {

    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false, unique = true)
    private UUID userId;

    @Column(name = "otp_hash", nullable = false)
    private String otpHash;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private boolean used;

    @Column(nullable = false)
    private int attempts;

    @Column(name = "reset_token_hash")
    private String resetTokenHash;

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

    public void setResetToken(String resetToken) {
        this.resetTokenHash = resetToken;
        this.resetTokenExpiresAt = LocalDateTime.now().plusMinutes(5);
    }

    public boolean isResetTokenExpired() {
        return resetTokenExpiresAt == null || LocalDateTime.now().isAfter(resetTokenExpiresAt);
    }

    public void resetFor(String newOtpHash) {
        this.otpHash = newOtpHash;
        this.resetTokenHash = null;
        this.resetTokenExpiresAt = null;
        this.used = false;
        this.attempts = 0;
        this.expiresAt = LocalDateTime.now().plusMinutes(5);
        this.createdAt = LocalDateTime.now();
    }

}
