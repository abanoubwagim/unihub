package com.unihub.identity.application.impl;

import com.unihub.identity.api.dto.ForgotPasswordRequest;
import com.unihub.identity.application.event.PasswordResetRequestedEvent;
import com.unihub.identity.domain.enums.AuthProvider;
import com.unihub.identity.domain.enums.Role;
import com.unihub.identity.domain.enums.UserStatus;
import com.unihub.identity.domain.model.PasswordResetToken;
import com.unihub.identity.domain.model.User;
import com.unihub.identity.domain.repository.PasswordResetTokenRepository;
import com.unihub.identity.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ForgotPasswordUseCase Tests")
class ForgotPasswordUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordResetTokenRepository tokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private ForgotPasswordUseCaseImpl forgotPasswordUseCase;

    private User activeUser;
    private final UUID userId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        activeUser = User.builder()
                .id(userId)
                .email("test@example.com")
                .passwordHash("hashed")
                .role(Role.STUDENT)
                .status(UserStatus.ACTIVE)
                .authProvider(AuthProvider.LOCAL)
                .emailVerified(true)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("should create new token and send email when no existing token")
    void shouldCreateNewTokenAndSendEmail() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(activeUser));
        when(tokenRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("hashedOtp");
        when(tokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        assertThatNoException().isThrownBy(
                () -> forgotPasswordUseCase.forgotPassword(new ForgotPasswordRequest("test@example.com")));

        ArgumentCaptor<PasswordResetToken> captor = ArgumentCaptor.forClass(PasswordResetToken.class);
        verify(tokenRepository).save(captor.capture());
        assertThat(captor.getValue().getUserId()).isEqualTo(userId);
        assertThat(captor.getValue().getOtpHash()).isEqualTo("hashedOtp");
        assertThat(captor.getValue().isUsed()).isFalse();
        assertThat(captor.getValue().getAttempts()).isZero();

        ArgumentCaptor<PasswordResetRequestedEvent> eventCaptor = ArgumentCaptor
                .forClass(PasswordResetRequestedEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        assertThat(eventCaptor.getValue().email()).isEqualTo("test@example.com");
        assertThat(eventCaptor.getValue().otp()).isNotBlank();
    }

    @Test
    @DisplayName("should silently do nothing when email is not registered")
    void shouldDoNothingWhenEmailNotFound() {
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        assertThatNoException().isThrownBy(
                () -> forgotPasswordUseCase.forgotPassword(new ForgotPasswordRequest("unknown@example.com")));

        verify(tokenRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("should throw BadRequestException when requesting too soon (rate limit)")
    void shouldThrowWhenRateLimitHit() {
        PasswordResetToken recentToken = PasswordResetToken.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .otpHash("oldHash")
                .expiresAt(LocalDateTime.now().plusMinutes(5))
                .createdAt(LocalDateTime.now().minusSeconds(30)) // only 30s ago — too soon
                .used(false)
                .attempts(0)
                .build();

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(activeUser));
        when(tokenRepository.findByUserId(userId)).thenReturn(Optional.of(recentToken));

        assertThatNoException().isThrownBy(
                () -> forgotPasswordUseCase.forgotPassword(new ForgotPasswordRequest("test@example.com")));

        verify(tokenRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("should update existing token and send email when rate limit has passed")
    void shouldUpdateExistingTokenWhenRateLimitPassed() {
        PasswordResetToken oldToken = PasswordResetToken.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .otpHash("oldHash")
                .expiresAt(LocalDateTime.now().minusMinutes(5))
                .createdAt(LocalDateTime.now().minusMinutes(5)) // 5 minutes ago — cooldown passed
                .used(true)
                .attempts(2)
                .build();

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(activeUser));
        when(tokenRepository.findByUserId(userId)).thenReturn(Optional.of(oldToken));
        when(passwordEncoder.encode(anyString())).thenReturn("newHashedOtp");
        when(tokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        assertThatNoException().isThrownBy(
                () -> forgotPasswordUseCase.forgotPassword(new ForgotPasswordRequest("test@example.com")));

        verify(tokenRepository).save(same(oldToken));
        verify(eventPublisher).publishEvent(any(PasswordResetRequestedEvent.class));
    }

    @Test
    @DisplayName("should update token fields when reusing existing token")
    void shouldResetTokenFieldsOnUpdate() {
        PasswordResetToken oldToken = PasswordResetToken.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .otpHash("oldHash")
                .expiresAt(LocalDateTime.now().minusMinutes(10))
                .createdAt(LocalDateTime.now().minusMinutes(10))
                .used(true)
                .attempts(3)
                .build();

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(activeUser));
        when(tokenRepository.findByUserId(userId)).thenReturn(Optional.of(oldToken));
        when(passwordEncoder.encode(anyString())).thenReturn("freshHash");
        when(tokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        forgotPasswordUseCase.forgotPassword(new ForgotPasswordRequest("test@example.com"));

        assertThat(oldToken.getOtpHash()).isEqualTo("freshHash");
        assertThat(oldToken.isUsed()).isFalse();
        assertThat(oldToken.getAttempts()).isZero();
        assertThat(oldToken.getResetToken()).isNull();
        assertThat(oldToken.getExpiresAt()).isAfter(LocalDateTime.now());
    }

    @Test
    @DisplayName("should normalize email (trim + lowercase) before lookup")
    void shouldNormalizeEmail() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());

        forgotPasswordUseCase.forgotPassword(new ForgotPasswordRequest("  TEST@EXAMPLE.COM  "));

        verify(userRepository).findByEmail("test@example.com");
    }

    @Test
    @DisplayName("should encode the OTP before saving")
    void shouldEncodeOtpBeforeSaving() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(activeUser));
        when(tokenRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("bcryptHash");
        when(tokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        forgotPasswordUseCase.forgotPassword(new ForgotPasswordRequest("test@example.com"));

        ArgumentCaptor<PasswordResetToken> captor = ArgumentCaptor.forClass(PasswordResetToken.class);
        verify(tokenRepository).save(captor.capture());
        assertThat(captor.getValue().getOtpHash()).isEqualTo("bcryptHash");
        assertThat(captor.getValue().getOtpHash()).doesNotMatch("\\d{6}");
    }
}