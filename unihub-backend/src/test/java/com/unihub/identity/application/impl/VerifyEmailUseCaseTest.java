package com.unihub.identity.application.impl;

import com.unihub.identity.api.dto.VerifyEmailRequest;
import com.unihub.identity.domain.model.EmailVerificationToken;
import com.unihub.identity.domain.model.User;
import com.unihub.identity.domain.enums.Role;
import com.unihub.identity.domain.enums.UserStatus;
import com.unihub.identity.domain.enums.AuthProvider;
import com.unihub.identity.domain.repository.EmailVerificationTokenRepository;
import com.unihub.identity.domain.repository.UserRepository;
import com.unihub.shared.exception.BadRequestException;
import com.unihub.shared.exception.NotFoundException;
import com.unihub.shared.exception.UnauthorizedException;

import org.junit.jupiter.api.BeforeEach;
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
@DisplayName("VerifyEmailUseCase Tests")
class VerifyEmailUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmailVerificationTokenRepository tokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private VerifyEmailUseCaseImpl verifyEmailUseCase;

    private User pendingUser;
    private VerifyEmailRequest validRequest;
    private final UUID userId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        pendingUser = User.builder()
                .id(userId)
                .email("student@example.com")
                .passwordHash("hashed")
                .role(Role.STUDENT)
                .status(UserStatus.PENDING)
                .authProvider(AuthProvider.LOCAL)
                .emailVerified(false)
                .createdAt(LocalDateTime.now())
                .build();

        validRequest = new VerifyEmailRequest("student@example.com", "123456");
    }

    private EmailVerificationToken buildToken(boolean used, boolean expired, int attempts) {
        return EmailVerificationToken.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .otpHash("hashedOtp")
                .expiresAt(expired
                        ? LocalDateTime.now().minusMinutes(1)
                        : LocalDateTime.now().plusMinutes(5))
                .used(used)
                .attempts(attempts)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("should verify email successfully with correct OTP")
    void shouldVerifyEmailSuccessfully() {

        EmailVerificationToken token = buildToken(false, false, 0);

        when(userRepository.findByEmail("student@example.com")).thenReturn(Optional.of(pendingUser));
        when(tokenRepository.findByUserId(userId)).thenReturn(Optional.of(token));
        when(passwordEncoder.matches("123456", "hashedOtp")).thenReturn(true);
        when(tokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        assertThatNoException().isThrownBy(() -> verifyEmailUseCase.verifyEmail(validRequest));


        verify(userRepository).save(argThat(u -> u.isEmailVerified() && u.getStatus() == UserStatus.ACTIVE));

        
        ArgumentCaptor<EmailVerificationToken> tokenCaptor = ArgumentCaptor.forClass(EmailVerificationToken.class);
        verify(tokenRepository).save(tokenCaptor.capture());

        EmailVerificationToken savedToken = tokenCaptor.getValue();
        assertThat(savedToken.isUsed()).isTrue();
        assertThat(savedToken.getAttempts()).isEqualTo(1);
    }


    @Test
    @DisplayName("should throw NotFoundException when user does not exist")
    void shouldThrowWhenUserNotFound() {

        when(userRepository.findByEmail("student@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> verifyEmailUseCase.verifyEmail(validRequest))
                .isInstanceOf(NotFoundException.class);

        
        verify(tokenRepository, never()).findByUserId(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("should throw BadRequestException when email is already verified")
    void shouldThrowWhenAlreadyVerified() {

        User alreadyVerifiedUser = User.builder()
                .id(userId).email("student@example.com")
                .passwordHash("hashed").role(Role.STUDENT)
                .status(UserStatus.ACTIVE).authProvider(AuthProvider.LOCAL)
                .emailVerified(true).createdAt(LocalDateTime.now()).build();

        when(userRepository.findByEmail("student@example.com"))
                .thenReturn(Optional.of(alreadyVerifiedUser));

        assertThatThrownBy(() -> verifyEmailUseCase.verifyEmail(validRequest))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("already verified");

        verify(tokenRepository, never()).findByUserId(any());
    }

    @Test
    @DisplayName("should throw BadRequestException when no token found for user")
    void shouldThrowWhenNoTokenFound() {

        when(userRepository.findByEmail("student@example.com")).thenReturn(Optional.of(pendingUser));
        when(tokenRepository.findByUserId(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> verifyEmailUseCase.verifyEmail(validRequest))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("should throw BadRequestException when token is already used")
    void shouldThrowWhenTokenAlreadyUsed() {
        EmailVerificationToken usedToken = buildToken(true, false, 0);

        when(userRepository.findByEmail("student@example.com")).thenReturn(Optional.of(pendingUser));
        when(tokenRepository.findByUserId(userId)).thenReturn(Optional.of(usedToken));

        assertThatThrownBy(() -> verifyEmailUseCase.verifyEmail(validRequest))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("already used");
    }

    @Test
    @DisplayName("should throw BadRequestException when token is expired")
    void shouldThrowWhenTokenIsExpired() {
        EmailVerificationToken expiredToken = buildToken(false, true, 0);

        when(userRepository.findByEmail("student@example.com")).thenReturn(Optional.of(pendingUser));
        when(tokenRepository.findByUserId(userId)).thenReturn(Optional.of(expiredToken));

        assertThatThrownBy(() -> verifyEmailUseCase.verifyEmail(validRequest))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("expired");
    }

    @Test
    @DisplayName("should throw UnauthorizedException when max attempts (5) reached — and NOT save the token")
    void shouldThrowWhenMaxAttemptsReached() {
        EmailVerificationToken lockedToken = buildToken(false, false, 5);

        when(userRepository.findByEmail("student@example.com")).thenReturn(Optional.of(pendingUser));
        when(tokenRepository.findByUserId(userId)).thenReturn(Optional.of(lockedToken));

        assertThatThrownBy(() -> verifyEmailUseCase.verifyEmail(validRequest))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("Too many");

        verify(tokenRepository, never()).save(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("should throw BadRequestException and increment attempts when OTP is wrong")
    void shouldThrowAndIncrementAttemptsWhenOtpWrong() {
        EmailVerificationToken token = buildToken(false, false, 2);

        when(userRepository.findByEmail("student@example.com")).thenReturn(Optional.of(pendingUser));
        when(tokenRepository.findByUserId(userId)).thenReturn(Optional.of(token));
        when(passwordEncoder.matches("123456", "hashedOtp")).thenReturn(false);

        assertThatThrownBy(() -> verifyEmailUseCase.verifyEmail(validRequest))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Invalid");

        verify(tokenRepository).save(argThat(t -> t.getAttempts() == 3));

        verify(userRepository, never()).save(any());
    }
}