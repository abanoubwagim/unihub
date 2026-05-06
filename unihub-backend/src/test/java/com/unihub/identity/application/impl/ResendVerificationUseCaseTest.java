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

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ResendVerificationUseCase Tests")
class ResendVerificationUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private EmailVerificationTokenRepository tokenRepository;

    @InjectMocks
    private ResendVerificationUseCaseImpl resendVerificationUseCase;

    private final UUID userId = UUID.randomUUID();
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
    @DisplayName("should throw BadRequestException when email is already verified")
    void shouldThrowWhenEmailAlreadyVerified() {
        User verifiedUser = User.builder()
                .id(userId).email("student@example.com")
                .role(Role.STUDENT).status(UserStatus.ACTIVE)
                .authProvider(AuthProvider.LOCAL).emailVerified(true)
                .createdAt(LocalDateTime.now()).build();

        when(userRepository.findByEmail("student@example.com")).thenReturn(Optional.of(verifiedUser));

        assertThatThrownBy(() -> resendVerificationUseCase.resendVerification(
                new ResendVerificationRequest("student@example.com")))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("already verified");

        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("should throw BadRequestException when email is not registered")
    void shouldThrowWhenEmailNotFound() {
        when(userRepository.findByEmail("student@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> resendVerificationUseCase.resendVerification(
                new ResendVerificationRequest("student@example.com")))
                .isInstanceOf(BadRequestException.class);

        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("should throw BadRequestException when token was requested within the last minute")
    void shouldThrowWhenRateLimitHit() {
        EmailVerificationToken recentToken = EmailVerificationToken.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .otpHash("hash")
                .expiresAt(LocalDateTime.now().plusMinutes(5))
                .used(false)
                .attempts(0)
                .createdAt(LocalDateTime.now().minusSeconds(20)) // 20s ago — too soon
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
    @DisplayName("should allow resend when previous token is older than 1 minute")
    void shouldAllowResendAfterCooldown() {
        EmailVerificationToken oldToken = EmailVerificationToken.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .otpHash("oldHash")
                .expiresAt(LocalDateTime.now().minusMinutes(5))
                .used(true)
                .attempts(2)
                .createdAt(LocalDateTime.now().minusMinutes(3)) // 3 minutes ago — cooldown passed
                .build();

        when(userRepository.findByEmail("student@example.com")).thenReturn(Optional.of(unverifiedUser));
        when(tokenRepository.findByUserId(userId)).thenReturn(Optional.of(oldToken));

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

        resendVerificationUseCase.resendVerification(new ResendVerificationRequest("student@example.com"));

        ArgumentCaptor<EmailVerificationRequestedEvent> captor = ArgumentCaptor
                .forClass(EmailVerificationRequestedEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());
        assertThat(captor.getValue().otp()).matches("\\d{6}");
    }
}