package com.unihub.identity.application.impl;

import com.unihub.identity.api.dto.ResetPasswordRequest;
import com.unihub.identity.domain.enums.AuthProvider;
import com.unihub.identity.domain.enums.Role;
import com.unihub.identity.domain.enums.UserStatus;
import com.unihub.identity.domain.model.PasswordResetToken;
import com.unihub.identity.domain.model.User;
import com.unihub.identity.domain.repository.PasswordResetTokenRepository;
import com.unihub.identity.domain.repository.UserRepository;
import com.unihub.shared.exception.BadRequestException;
import com.unihub.shared.util.TokenHashUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ResetPasswordUseCase Tests")
class ResetPasswordUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordResetTokenRepository tokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private ResetPasswordUseCaseImpl resetPasswordUseCase;

    private final UUID userId = UUID.randomUUID();
    private final String plainResetToken = UUID.randomUUID().toString();
    private final String hashedResetToken = TokenHashUtil.sha256(plainResetToken);

    private User buildUser() {
        return User.builder()
                .id(userId)
                .email("user@example.com")
                .passwordHash("oldHashedPassword")
                .role(Role.STUDENT)
                .status(UserStatus.ACTIVE)
                .authProvider(AuthProvider.LOCAL)
                .emailVerified(true)
                .createdAt(LocalDateTime.now())
                .build();
    }

    private PasswordResetToken buildValidToken() {

        PasswordResetToken token = PasswordResetToken.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .otpHash("someOtpHash")
                .expiresAt(LocalDateTime.now().plusMinutes(5))
                .createdAt(LocalDateTime.now())
                .used(true)
                .attempts(1)
                .build();
        token.setResetToken(hashedResetToken);
        return token;
    }

    @Test
    @DisplayName("should reset password successfully with valid token")
    void shouldResetPasswordSuccessfully() {
        PasswordResetToken token = buildValidToken();
        User user = buildUser();

        when(tokenRepository.findByResetToken(hashedResetToken)).thenReturn(Optional.of(token));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("NewPass1@")).thenReturn("newBcryptHash");
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        assertThatNoException().isThrownBy(
                () -> resetPasswordUseCase.resetPassword(
                        new ResetPasswordRequest(plainResetToken, "NewPass1@", "NewPass1@")));

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getPasswordHash()).isEqualTo("newBcryptHash");
    }

    @Test
    @DisplayName("should throw BadRequestException when passwords do not match")
    void shouldThrowWhenPasswordsDoNotMatch() {
        assertThatThrownBy(() -> resetPasswordUseCase.resetPassword(
                new ResetPasswordRequest(plainResetToken, "NewPass1@", "Mismatch2@")))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("match");

        verify(tokenRepository, never()).findByResetToken(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("should throw BadRequestException when reset token does not exist")
    void shouldThrowWhenTokenNotFound() {
        when(tokenRepository.findByResetToken(hashedResetToken)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> resetPasswordUseCase.resetPassword(
                new ResetPasswordRequest(plainResetToken, "NewPass1@", "NewPass1@")))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Invalid");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("should throw BadRequestException when reset token is expired")
    void shouldThrowWhenResetTokenExpired() {
        PasswordResetToken expiredToken = PasswordResetToken.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .otpHash("hash")
                .expiresAt(LocalDateTime.now().plusMinutes(5))
                .createdAt(LocalDateTime.now())
                .used(true)
                .attempts(1)
                .resetToken(hashedResetToken)
                .resetTokenExpiresAt(LocalDateTime.now().minusMinutes(1)) // expired
                .build();

        when(tokenRepository.findByResetToken(hashedResetToken)).thenReturn(Optional.of(expiredToken));

        assertThatThrownBy(() -> resetPasswordUseCase.resetPassword(
                new ResetPasswordRequest(plainResetToken, "NewPass1@", "NewPass1@")))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("expired");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("should delete token after successful password reset")
    void shouldDeleteTokenAfterSuccessfulReset() {
        PasswordResetToken token = buildValidToken();
        User user = buildUser();

        when(tokenRepository.findByResetToken(hashedResetToken)).thenReturn(Optional.of(token));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode(anyString())).thenReturn("newHash");
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        resetPasswordUseCase.resetPassword(
                new ResetPasswordRequest(plainResetToken, "NewPass1@", "NewPass1@"));

        verify(tokenRepository).deleteByUserId(userId);
    }

    @Test
    @DisplayName("should throw BadRequestException when resetTokenExpiresAt is null")
    void shouldThrowWhenResetTokenExpiresAtIsNull() {
        PasswordResetToken tokenWithNullExpiry = PasswordResetToken.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .otpHash("hash")
                .expiresAt(LocalDateTime.now().plusMinutes(5))
                .createdAt(LocalDateTime.now())
                .used(true)
                .attempts(1)
                .resetToken(hashedResetToken)
                .resetTokenExpiresAt(null) // null means expired per isResetTokenExpired()
                .build();

        when(tokenRepository.findByResetToken(hashedResetToken)).thenReturn(Optional.of(tokenWithNullExpiry));

        assertThatThrownBy(() -> resetPasswordUseCase.resetPassword(
                new ResetPasswordRequest(plainResetToken, "NewPass1@", "NewPass1@")))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("expired");
    }

    @Test
    @DisplayName("should encode new password before saving — raw password must not be stored")
    void shouldEncodeNewPassword() {
        PasswordResetToken token = buildValidToken();
        User user = buildUser();

        when(tokenRepository.findByResetToken(hashedResetToken)).thenReturn(Optional.of(token));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("NewPass1@")).thenReturn("$2a$10$hashedValue");
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        resetPasswordUseCase.resetPassword(
                new ResetPasswordRequest(plainResetToken, "NewPass1@", "NewPass1@"));

        verify(passwordEncoder).encode("NewPass1@");
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getPasswordHash()).doesNotContain("NewPass1@");
    }
}