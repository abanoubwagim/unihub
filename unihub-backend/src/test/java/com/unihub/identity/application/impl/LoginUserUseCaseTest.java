package com.unihub.identity.application.impl;

import com.unihub.identity.api.dto.req.LoginRequest;
import com.unihub.identity.api.dto.res.AuthenticationResult;
import com.unihub.identity.application.usecase.RefreshTokenUseCase;
import com.unihub.identity.domain.enums.AuthProvider;
import com.unihub.identity.domain.enums.Role;
import com.unihub.identity.domain.enums.UserStatus;
import com.unihub.identity.domain.model.RefreshToken;
import com.unihub.identity.domain.model.User;
import com.unihub.identity.domain.repository.UserRepository;
import com.unihub.shared.exception.UnauthorizedException;
import com.unihub.shared.security.JwtSubject;
import com.unihub.shared.security.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
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

    @Mock
    private RefreshTokenUseCase refreshTokenUseCase;

    @InjectMocks
    private LoginUserUseCaseImpl loginUserUseCase;

    private User activeVerifiedUser;
    private LoginRequest validRequest;

    private User buildUser(AuthProvider provider, boolean emailVerified, UserStatus status) {
        return User.builder()
                .email("test@example.com")
                .passwordHash("hashedPassword")
                .role(Role.STUDENT)
                .status(status)
                .authProvider(provider)
                .emailVerified(emailVerified)
                .createdAt(LocalDateTime.now())
                .build();
    }

    private RefreshTokenUseCase.CreationResult stubRefreshCreation() {
        RefreshToken entity = RefreshToken.builder()
                .id(UUID.randomUUID())
                .userId(UUID.randomUUID())
                .tokenHash("someHash")
                .expiresAt(Instant.now().plusSeconds(604800))
                .revoked(false)
                .build();
        return new RefreshTokenUseCase.CreationResult(entity, "raw-refresh-token");
    }

    @BeforeEach
    void setUp() {
        activeVerifiedUser = buildUser(AuthProvider.LOCAL, true, UserStatus.ACTIVE);
        validRequest = new LoginRequest("test@example.com", "rawPassword");
    }

    @Test
    @DisplayName("should return AuthenticationResult with access token when credentials are correct")
    void shouldReturnAuthenticationResultWhenCredentialsAreCorrect() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(activeVerifiedUser));
        when(passwordEncoder.matches("rawPassword", "hashedPassword")).thenReturn(true);
        when(jwtService.generateToken(any(JwtSubject.class))).thenReturn("mocked.jwt.token");
        when(jwtService.getExpirationSeconds("mocked.jwt.token")).thenReturn(900L);
        when(refreshTokenUseCase.create(any())).thenReturn(stubRefreshCreation());

        AuthenticationResult result = loginUserUseCase.login(validRequest);   // ← AuthenticationResult

        assertThat(result).isNotNull();
        assertThat(result.accessToken()).isEqualTo("mocked.jwt.token");
        assertThat(result.rawRefreshToken()).isEqualTo("raw-refresh-token");
        assertThat(result.expiresIn()).isEqualTo(900L);

        verify(userRepository).findByEmail("test@example.com");
        verify(jwtService).generateToken(any(JwtSubject.class));
        verify(refreshTokenUseCase).create(any());
    }

    @Test
    @DisplayName("should throw UnauthorizedException when user does not exist")
    void shouldThrowWhenUserNotFound() {
        when(userRepository.findByEmail(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> loginUserUseCase.login(validRequest))
                .isInstanceOf(UnauthorizedException.class);

        verify(jwtService, never()).generateToken(any());
        verify(refreshTokenUseCase, never()).create(any());
    }

    @Test
    @DisplayName("should throw UnauthorizedException when password is wrong")
    void shouldThrowWhenPasswordIsWrong() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(activeVerifiedUser));
        when(passwordEncoder.matches("rawPassword", "hashedPassword")).thenReturn(false);

        assertThatThrownBy(() -> loginUserUseCase.login(validRequest))
                .isInstanceOf(UnauthorizedException.class);

        verify(jwtService, never()).generateToken(any());
        verify(refreshTokenUseCase, never()).create(any());
    }

    @Test
    @DisplayName("should throw UnauthorizedException when email is not verified")
    void shouldThrowWhenEmailNotVerified() {
        User unverifiedUser = buildUser(AuthProvider.LOCAL, false, UserStatus.ACTIVE);

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(unverifiedUser));
        when(passwordEncoder.matches("rawPassword", "hashedPassword")).thenReturn(true);

        assertThatThrownBy(() -> loginUserUseCase.login(validRequest))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("verified");

        verify(jwtService, never()).generateToken(any());
        verify(refreshTokenUseCase, never()).create(any());
    }

    @Test
    @DisplayName("should throw UnauthorizedException when user is banned")
    void shouldThrowWhenUserIsBanned() {
        User bannedUser = buildUser(AuthProvider.LOCAL, true, UserStatus.BANNED);

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(bannedUser));
        when(passwordEncoder.matches("rawPassword", "hashedPassword")).thenReturn(true);

        assertThatThrownBy(() -> loginUserUseCase.login(validRequest))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("banned");

        verify(jwtService, never()).generateToken(any());
        verify(refreshTokenUseCase, never()).create(any());
    }

    @Test
    @DisplayName("should throw UnauthorizedException when user is suspended")
    void shouldThrowWhenUserIsSuspended() {
        User suspendedUser = buildUser(AuthProvider.LOCAL, true, UserStatus.SUSPENDED);

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(suspendedUser));
        when(passwordEncoder.matches("rawPassword", "hashedPassword")).thenReturn(true);

        assertThatThrownBy(() -> loginUserUseCase.login(validRequest))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("suspended");

        verify(jwtService, never()).generateToken(any());
        verify(refreshTokenUseCase, never()).create(any());
    }

    @Test
    @DisplayName("should throw UnauthorizedException when Google OAuth user tries to login with password")
    void shouldThrowWhenGoogleOAuthUserTriesToLoginWithPassword() {
        User googleUser = buildUser(AuthProvider.GOOGLE, true, UserStatus.ACTIVE);

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(googleUser));

        assertThatThrownBy(() -> loginUserUseCase.login(validRequest))
                .isInstanceOf(UnauthorizedException.class);

        // Password check and token generation must NEVER be reached
        verify(passwordEncoder, never()).matches(any(), any());
        verify(jwtService, never()).generateToken(any());
        verify(refreshTokenUseCase, never()).create(any());
    }

    @Test
    @DisplayName("should throw UnauthorizedException when Microsoft OAuth user tries to login with password")
    void shouldThrowWhenMicrosoftOAuthUserTriesToLoginWithPassword() {
        User microsoftUser = buildUser(AuthProvider.MICROSOFT, true, UserStatus.ACTIVE);

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(microsoftUser));

        assertThatThrownBy(() -> loginUserUseCase.login(validRequest))
                .isInstanceOf(UnauthorizedException.class);

        verify(passwordEncoder, never()).matches(any(), any());
        verify(jwtService, never()).generateToken(any());
        verify(refreshTokenUseCase, never()).create(any());
    }

    @Test
    @DisplayName("should normalize email (trim + lowercase) before lookup")
    void shouldNormalizeEmailBeforeLookup() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(activeVerifiedUser));
        when(passwordEncoder.matches("rawPassword", "hashedPassword")).thenReturn(true);
        when(jwtService.generateToken(any())).thenReturn("token");
        when(jwtService.getExpirationSeconds("token")).thenReturn(900L);
        when(refreshTokenUseCase.create(any())).thenReturn(stubRefreshCreation());

        LoginRequest messyEmailRequest = new LoginRequest("  TEST@EXAMPLE.COM  ", "rawPassword");
        loginUserUseCase.login(messyEmailRequest);

        // Must query with the normalized form, not the raw input
        verify(userRepository).findByEmail("test@example.com");
    }
}