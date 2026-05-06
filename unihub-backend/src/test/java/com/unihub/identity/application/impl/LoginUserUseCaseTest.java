package com.unihub.identity.application.impl;

import com.unihub.identity.api.dto.LoginRequest;
import com.unihub.identity.api.dto.LoginResponse;
import com.unihub.identity.domain.model.User;
import com.unihub.identity.domain.enums.Role;
import com.unihub.identity.domain.enums.UserStatus;
import com.unihub.identity.domain.enums.AuthProvider;
import com.unihub.identity.domain.repository.UserRepository;
import com.unihub.shared.exception.UnauthorizedException;
import com.unihub.shared.security.JwtService;
import com.unihub.shared.security.JwtSubject;

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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("LoginUserUseCase Tests")
class LoginUserUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private LoginUserUseCaseImpl loginUserUseCase;

    private User activeVerifiedUser;
    private LoginRequest validRequest;

    private User buildUser(boolean emailVerified, UserStatus status) {
        return User.builder()
                .id(UUID.randomUUID())
                .email("test@example.com")
                .passwordHash("hashedPassword")
                .role(Role.STUDENT)
                .status(status)
                .authProvider(AuthProvider.LOCAL)
                .emailVerified(emailVerified)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @BeforeEach
    void setUp() {
        activeVerifiedUser = buildUser(true, UserStatus.ACTIVE);
        validRequest = new LoginRequest("test@example.com", "rawPassword");
    }

    @Test
    @DisplayName("should return token when credentials are correct")
    void shouldReturnTokenWhenCredentialsAreCorrect() {

        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(activeVerifiedUser));

        when(passwordEncoder.matches("rawPassword", "hashedPassword"))
                .thenReturn(true);

        when(jwtService.generateToken(any(JwtSubject.class)))
                .thenReturn("mocked.jwt.token");

        LoginResponse response = loginUserUseCase.login(validRequest);

        assertThat(response).isNotNull();
        assertThat(response.accessToken()).isEqualTo("mocked.jwt.token");
        assertThat(response.tokenType()).isEqualTo("Bearer");

        verify(userRepository).findByEmail("test@example.com");
        verify(jwtService).generateToken(any(JwtSubject.class));
    }

    @Test
    @DisplayName("should throw when user does not exist")
    void shouldThrowWhenUserNotFound() {
        when(userRepository.findByEmail(any()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> loginUserUseCase.login(validRequest))
                .isInstanceOf(UnauthorizedException.class);

        verify(jwtService, never()).generateToken(any());
    }

    @Test
    @DisplayName("should throw when password is wrong")
    void shouldThrowWhenPasswordIsWrong() {

        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(activeVerifiedUser));
        when(passwordEncoder.matches("rawPassword", "hashedPassword"))
                .thenReturn(false);

        assertThatThrownBy(() -> loginUserUseCase.login(validRequest))
                .isInstanceOf(UnauthorizedException.class);

        verify(jwtService, never()).generateToken(any());
    }

    @Test
    @DisplayName("should throw when email is not verified")
    void shouldThrowWhenEmailNotVerified() {
        User unverifiedUser = buildUser(false, UserStatus.ACTIVE);

        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(unverifiedUser));
        when(passwordEncoder.matches("rawPassword", "hashedPassword"))
                .thenReturn(true);

        assertThatThrownBy(() -> loginUserUseCase.login(validRequest))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("verified");

        verify(jwtService, never()).generateToken(any());
    }

    @Test
    @DisplayName("should throw when user is banned")
    void shouldThrowWhenUserIsBanned() {
        User bannedUser = buildUser(true, UserStatus.BANNED);

        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(bannedUser));
        when(passwordEncoder.matches("rawPassword", "hashedPassword"))
                .thenReturn(true);

        assertThatThrownBy(() -> loginUserUseCase.login(validRequest))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("banned");

        verify(jwtService, never()).generateToken(any());
    }

    @Test
    @DisplayName("should throw when user is suspended")
    void shouldThrowWhenUserIsSuspended() {

        User suspendedUser = buildUser(true, UserStatus.SUSPENDED);

        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(suspendedUser));
        when(passwordEncoder.matches("rawPassword", "hashedPassword"))
                .thenReturn(true);

        assertThatThrownBy(() -> loginUserUseCase.login(validRequest))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("suspended");

        verify(jwtService, never()).generateToken(any());
    }

    @Test
    @DisplayName("should throw UnauthorizedException when Google OAuth user tries to login with password")
    void shouldThrowWhenOAuthUserTriesToLoginWithPassword() {
        User googleUser = User.builder()
                .id(UUID.randomUUID())
                .email("test@example.com")
                .passwordHash(null) // OAuth users have no password
                .role(Role.STUDENT)
                .status(UserStatus.ACTIVE)
                .authProvider(AuthProvider.GOOGLE)
                .emailVerified(true)
                .createdAt(LocalDateTime.now())
                .build();

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(googleUser));

        assertThatThrownBy(() -> loginUserUseCase.login(validRequest))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("google");

        // Password check and token generation must NEVER be reached
        verify(passwordEncoder, never()).matches(any(), any());
        verify(jwtService, never()).generateToken(any());
    }

    @Test
    @DisplayName("should throw UnauthorizedException when Microsoft OAuth user tries to login with password")
    void shouldThrowWhenMicrosoftOAuthUserTriesToLoginWithPassword() {
        User microsoftUser = User.builder()
                .id(UUID.randomUUID())
                .email("test@example.com")
                .passwordHash(null)
                .role(Role.STUDENT)
                .status(UserStatus.ACTIVE)
                .authProvider(AuthProvider.MICROSOFT)
                .emailVerified(true)
                .createdAt(LocalDateTime.now())
                .build();

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(microsoftUser));

        assertThatThrownBy(() -> loginUserUseCase.login(validRequest))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("microsoft");

        verify(passwordEncoder, never()).matches(any(), any());
        verify(jwtService, never()).generateToken(any());
    }

    @Test
    @DisplayName("should normalize email (trim + lowercase) before lookup")
    void shouldNormalizeEmailBeforeLookup() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(activeVerifiedUser));
        when(passwordEncoder.matches("rawPassword", "hashedPassword")).thenReturn(true);
        when(jwtService.generateToken(any())).thenReturn("token");

        LoginRequest messyEmailRequest = new LoginRequest("  TEST@EXAMPLE.COM  ", "rawPassword");
        loginUserUseCase.login(messyEmailRequest);

        // Must query with the normalized form, not the raw input
        verify(userRepository).findByEmail("test@example.com");
    }
}