package com.unihub.identity.application.impl;

import com.unihub.identity.api.dto.VerifyResetOtpRequest;
import com.unihub.identity.api.dto.VerifyResetOtpResponse;
import com.unihub.identity.domain.enums.AuthProvider;
import com.unihub.identity.domain.enums.Role;
import com.unihub.identity.domain.enums.UserStatus;
import com.unihub.identity.domain.model.PasswordResetToken;
import com.unihub.identity.domain.model.User;
import com.unihub.identity.domain.repository.PasswordResetTokenRepository;
import com.unihub.identity.domain.repository.UserRepository;
import com.unihub.shared.exception.BadRequestException;
import com.unihub.shared.exception.UnauthorizedException;
import com.unihub.shared.util.TokenHashUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
@DisplayName("VerifyResetOtpUseCase Tests")
class VerifyResetOtpUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordResetTokenRepository tokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private VerifyResetOtpUseCaseImpl verifyResetOtpUseCase;

    private final UUID userId = UUID.randomUUID();
    private User activeUser;
    private VerifyResetOtpRequest validRequest;

    @BeforeEach
    void setUp() {
        activeUser = User.builder()
                .id(userId)
                .email("user@example.com")
                .passwordHash("hash")
                .role(Role.STUDENT)
                .status(UserStatus.ACTIVE)
                .authProvider(AuthProvider.LOCAL)
                .emailVerified(true)
                .createdAt(LocalDateTime.now())
                .build();

        validRequest = new VerifyResetOtpRequest("user@example.com", "123456");
    }

    private PasswordResetToken buildToken(boolean used, boolean expired, int attempts) {
        return PasswordResetToken.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .otpHash("hashedOtp")
                .expiresAt(expired
                        ? LocalDateTime.now().minusMinutes(1)
                        : LocalDateTime.now().plusMinutes(5))
                .createdAt(LocalDateTime.now())
                .used(used)
                .attempts(attempts)
                .build();
    }

    @Test
    @DisplayName("should return reset token on correct OTP")
    void shouldReturnResetTokenOnCorrectOtp() {
        PasswordResetToken token = buildToken(false, false, 0);

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(activeUser));
        when(tokenRepository.findByUserId(userId)).thenReturn(Optional.of(token));
        when(passwordEncoder.matches("123456", "hashedOtp")).thenReturn(true);
        when(tokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        VerifyResetOtpResponse response = verifyResetOtpUseCase.verifyResetOtp(validRequest);

        assertThat(response).isNotNull();
        assertThat(response.resetToken()).isNotBlank();
    }

    @Test
    @DisplayName("should store HASHED reset token in DB, not the plain one")
    void shouldStoreHashedResetToken() {
        PasswordResetToken token = buildToken(false, false, 0);

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(activeUser));
        when(tokenRepository.findByUserId(userId)).thenReturn(Optional.of(token));
        when(passwordEncoder.matches("123456", "hashedOtp")).thenReturn(true);
        when(tokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        VerifyResetOtpResponse response = verifyResetOtpUseCase.verifyResetOtp(validRequest);

        // The stored reset token must be sha256 of the plain one returned to the user
        String expectedHash = TokenHashUtil.sha256(response.resetToken());
        assertThat(token.getResetToken()).isEqualTo(expectedHash);
    }

    @Test
    @DisplayName("should mark token as used after successful OTP verification")
    void shouldMarkTokenAsUsedAfterSuccess() {
        PasswordResetToken token = buildToken(false, false, 0);

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(activeUser));
        when(tokenRepository.findByUserId(userId)).thenReturn(Optional.of(token));
        when(passwordEncoder.matches("123456", "hashedOtp")).thenReturn(true);
        when(tokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        verifyResetOtpUseCase.verifyResetOtp(validRequest);

        assertThat(token.isUsed()).isTrue();
        verify(tokenRepository).save(token);
    }

    @Test
    @DisplayName("should throw BadRequestException when user not found (generic message)")
    void shouldThrowWhenUserNotFound() {
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> verifyResetOtpUseCase.verifyResetOtp(validRequest))
                .isInstanceOf(BadRequestException.class);

        verify(tokenRepository, never()).save(any());
    }

    @Test
    @DisplayName("should throw BadRequestException when token not found (generic message)")
    void shouldThrowWhenTokenNotFound() {
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(activeUser));
        when(tokenRepository.findByUserId(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> verifyResetOtpUseCase.verifyResetOtp(validRequest))
                .isInstanceOf(BadRequestException.class);

        verify(tokenRepository, never()).save(any());
    }

    @Test
    @DisplayName("should throw BadRequestException when token is already used")
    void shouldThrowWhenTokenAlreadyUsed() {
        PasswordResetToken usedToken = buildToken(true, false, 0);

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(activeUser));
        when(tokenRepository.findByUserId(userId)).thenReturn(Optional.of(usedToken));

        assertThatThrownBy(() -> verifyResetOtpUseCase.verifyResetOtp(validRequest))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("already used");
    }

    @Test
    @DisplayName("should throw BadRequestException when token is expired")
    void shouldThrowWhenTokenExpired() {
        PasswordResetToken expiredToken = buildToken(false, true, 0);

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(activeUser));
        when(tokenRepository.findByUserId(userId)).thenReturn(Optional.of(expiredToken));

        assertThatThrownBy(() -> verifyResetOtpUseCase.verifyResetOtp(validRequest))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("expired");
    }

    @Test
    @DisplayName("should throw UnauthorizedException when max attempts (5) reached")
    void shouldThrowWhenMaxAttemptsReached() {
        PasswordResetToken lockedToken = buildToken(false, false, 5);

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(activeUser));
        when(tokenRepository.findByUserId(userId)).thenReturn(Optional.of(lockedToken));

        assertThatThrownBy(() -> verifyResetOtpUseCase.verifyResetOtp(validRequest))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("Too many");
    }

    @Test
    @DisplayName("should increment attempts and throw when OTP is wrong")
    void shouldIncrementAttemptsAndThrowWhenOtpWrong() {
        PasswordResetToken token = buildToken(false, false, 2);

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(activeUser));
        when(tokenRepository.findByUserId(userId)).thenReturn(Optional.of(token));
        when(passwordEncoder.matches("123456", "hashedOtp")).thenReturn(false);

        assertThatThrownBy(() -> verifyResetOtpUseCase.verifyResetOtp(validRequest))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Invalid");

        verify(tokenRepository).save(argThat(t -> t.getAttempts() == 3));
    }

    @Test
    @DisplayName("should normalize email before lookup")
    void shouldNormalizeEmail() {
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> verifyResetOtpUseCase.verifyResetOtp(
                new VerifyResetOtpRequest("  USER@EXAMPLE.COM  ", "123456")))
                .isInstanceOf(BadRequestException.class);

        verify(userRepository).findByEmail("user@example.com");
    }
}