package com.unihub.identity.application.impl;

import com.unihub.identity.api.dto.ResendVerificationRequest;
import com.unihub.identity.application.event.EmailVerificationRequestedEvent;
import com.unihub.identity.domain.enums.AuthProvider;
import com.unihub.identity.domain.enums.Role;
import com.unihub.identity.domain.enums.UserStatus;
import com.unihub.identity.domain.model.EmailVerificationToken;
import com.unihub.identity.domain.model.User;
import com.unihub.identity.domain.repository.EmailVerificationTokenRepository;
import com.unihub.identity.domain.repository.UserRepository;
import com.unihub.shared.exception.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ResendVerificationUseCase Tests")
class ResendVerificationUseCaseTest {

    private final UUID userId = UUID.randomUUID();

    @Mock
    private UserRepository userRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private EmailVerificationTokenRepository tokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private ResendVerificationUseCaseImpl resendVerificationUseCase;

    private User unverifiedUser;

    @BeforeEach
    void setUp() {
        unverifiedUser = User.builder()
                .id(userId)
                .email("student@example.com")
                .role(Role.STUDENT)
                .status(UserStatus.PENDING)
                .authProvider(AuthProvider.LOCAL)
                .emailVerified(false)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("should publish EmailVerificationRequestedEvent when no existing token")
    void shouldPublishEventWhenNoExistingToken() {
        when(userRepository.findByEmail("student@example.com")).thenReturn(Optional.of(unverifiedUser));
        when(tokenRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("hashedOtp");

        assertThatNoException().isThrownBy(
                () -> resendVerificationUseCase.resendVerification(
                        new ResendVerificationRequest("student@example.com")));

        ArgumentCaptor<EmailVerificationRequestedEvent> captor = ArgumentCaptor
                .forClass(EmailVerificationRequestedEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());
        assertThat(captor.getValue().userId()).isEqualTo(userId);
        assertThat(captor.getValue().email()).isEqualTo("student@example.com");
        assertThat(captor.getValue().otp()).isNotBlank();
    }

    @Test
    @DisplayName("should do nothing silently when email is already verified")
    void shouldReturnSilentlyWhenEmailAlreadyVerified() {
        User verifiedUser = User.builder()
                .id(userId).email("student@example.com")
                .role(Role.STUDENT).status(UserStatus.ACTIVE)
                .authProvider(AuthProvider.LOCAL).emailVerified(true)
                .createdAt(LocalDateTime.now()).build();

        when(userRepository.findByEmail("student@example.com")).thenReturn(Optional.of(verifiedUser));

        assertThatNoException().isThrownBy(
                () -> resendVerificationUseCase.resendVerification(
                        new ResendVerificationRequest("student@example.com")));

        verify(eventPublisher, never()).publishEvent(any());
        verify(tokenRepository, never()).findByUserId(any());
    }

    @Test
    @DisplayName("should do nothing silently when email is not registered")
    void shouldReturnSilentlyWhenEmailNotFound() {
        when(userRepository.findByEmail("student@example.com")).thenReturn(Optional.empty());

        assertThatNoException().isThrownBy(
                () -> resendVerificationUseCase.resendVerification(
                        new ResendVerificationRequest("student@example.com")));

        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("should throw BadRequestException when token was requested within the rate-limit window")
    void shouldThrowWhenRateLimitHit() {
        EmailVerificationToken recentToken = EmailVerificationToken.builder()
                .userId(userId)
                .otpHash("hash")
                .expiresAt(LocalDateTime.now().plusMinutes(5))
                .used(false)
                .attempts(0)
                .createdAt(LocalDateTime.now().minusMinutes(1))
                .build();

        when(userRepository.findByEmail("student@example.com")).thenReturn(Optional.of(unverifiedUser));
        when(tokenRepository.findByUserId(userId)).thenReturn(Optional.of(recentToken));

        assertThatThrownBy(() -> resendVerificationUseCase.resendVerification(
                new ResendVerificationRequest("student@example.com")))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("wait");

        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("should allow resend when previous token is older than the rate-limit window")
    void shouldAllowResendAfterCooldown() {
        EmailVerificationToken oldToken = EmailVerificationToken.builder()
                .userId(userId)
                .otpHash("oldHash")
                .expiresAt(LocalDateTime.now().minusMinutes(5))
                .used(true)
                .attempts(2)
                .createdAt(LocalDateTime.now().minusMinutes(6))
                .build();

        when(userRepository.findByEmail("student@example.com")).thenReturn(Optional.of(unverifiedUser));
        when(tokenRepository.findByUserId(userId)).thenReturn(Optional.of(oldToken));
        when(passwordEncoder.encode(anyString())).thenReturn("newHash");

        assertThatNoException().isThrownBy(
                () -> resendVerificationUseCase.resendVerification(
                        new ResendVerificationRequest("student@example.com")));

        verify(eventPublisher).publishEvent(any(EmailVerificationRequestedEvent.class));
    }

    @Test
    @DisplayName("should publish event with a 6-digit OTP")
    void shouldPublishEventWithSixDigitOtp() {
        when(userRepository.findByEmail("student@example.com")).thenReturn(Optional.of(unverifiedUser));
        when(tokenRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("hashedOtp");

        resendVerificationUseCase.resendVerification(new ResendVerificationRequest("student@example.com"));

        ArgumentCaptor<EmailVerificationRequestedEvent> captor = ArgumentCaptor
                .forClass(EmailVerificationRequestedEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());
        assertThat(captor.getValue().otp()).matches("\\d{6}");
    }

    @Test
    @DisplayName("should refresh the existing token (reset otp, attempts, used flag) on resend")
    void shouldRefreshExistingToken() {
        EmailVerificationToken oldToken = EmailVerificationToken.builder()
                .userId(userId)
                .otpHash("oldHash")
                .expiresAt(LocalDateTime.now().minusMinutes(5))
                .used(true)
                .attempts(3)
                .createdAt(LocalDateTime.now().minusMinutes(10))
                .build();

        when(userRepository.findByEmail("student@example.com")).thenReturn(Optional.of(unverifiedUser));
        when(tokenRepository.findByUserId(userId)).thenReturn(Optional.of(oldToken));
        when(passwordEncoder.encode(anyString())).thenReturn("freshHash");

        resendVerificationUseCase.resendVerification(new ResendVerificationRequest("student@example.com"));

        assertThat(oldToken.getAttempts()).isZero();
        assertThat(oldToken.isUsed()).isFalse();
        assertThat(oldToken.getOtpHash()).isEqualTo("freshHash");
        assertThat(oldToken.getExpiresAt()).isAfter(LocalDateTime.now());
    }

    @Test
    @DisplayName("should throw BadRequestException on concurrent resend — OptimisticLockingFailureException")
    void shouldThrowBadRequestOnOptimisticLockingConflict() {
        when(userRepository.findByEmail("student@example.com")).thenReturn(Optional.of(unverifiedUser));
        when(tokenRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("hashedOtp");
        when(tokenRepository.save(any()))
                .thenThrow(new OptimisticLockingFailureException("version conflict"));

        assertThatThrownBy(() -> resendVerificationUseCase.resendVerification(
                new ResendVerificationRequest("student@example.com")))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("wait");

        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("should throw BadRequestException on concurrent insert — DataIntegrityViolationException")
    void shouldThrowBadRequestOnDataIntegrityViolation() {
        when(userRepository.findByEmail("student@example.com")).thenReturn(Optional.of(unverifiedUser));
        when(tokenRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("hashedOtp");
        when(tokenRepository.save(any()))
                .thenThrow(new DataIntegrityViolationException("duplicate key"));

        assertThatThrownBy(() -> resendVerificationUseCase.resendVerification(
                new ResendVerificationRequest("student@example.com")))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("wait");

        verify(eventPublisher, never()).publishEvent(any());
    }
}