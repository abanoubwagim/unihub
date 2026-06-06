package com.unihub.identity.application.impl;

import com.unihub.identity.api.dto.res.UserResponse;
import com.unihub.identity.domain.enums.AuthProvider;
import com.unihub.identity.domain.enums.Role;
import com.unihub.identity.domain.enums.UserStatus;
import com.unihub.identity.domain.model.User;
import com.unihub.identity.domain.repository.UserRepository;
import com.unihub.shared.exception.UnauthorizedException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetCurrentUserUseCase Tests")
class GetCurrentUserUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private GetCurrentUserUseCaseImpl getCurrentUserUseCase;

    @Test
    @DisplayName("should return full UserResponse when user exists")
    void shouldReturnUserResponseWhenUserExists() {
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .email("student@example.com")
                .role(Role.STUDENT)
                .status(UserStatus.ACTIVE)
                .authProvider(AuthProvider.LOCAL)
                .emailVerified(true)
                .createdAt(LocalDateTime.now())
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        UserResponse response = getCurrentUserUseCase.getCurrentUser(userId);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(userId);
        assertThat(response.email()).isEqualTo("student@example.com");
        assertThat(response.role()).isEqualTo(Role.STUDENT);
        assertThat(response.status()).isEqualTo(UserStatus.ACTIVE);
        assertThat(response.emailVerified()).isTrue();
    }

    @Test
    @DisplayName("should throw UnauthorizedException when user does not exist")
    void shouldThrowWhenUserNotFound() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> getCurrentUserUseCase.getCurrentUser(userId))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    @DisplayName("should map emailVerified=false correctly for PENDING users")
    void shouldMapUnverifiedUserCorrectly() {
        UUID userId = UUID.randomUUID();
        User pendingUser = User.builder()
                .id(userId)
                .email("pending@example.com")
                .role(Role.STUDENT)
                .status(UserStatus.PENDING)
                .authProvider(AuthProvider.LOCAL)
                .emailVerified(false)
                .createdAt(LocalDateTime.now())
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(pendingUser));

        UserResponse response = getCurrentUserUseCase.getCurrentUser(userId);

        assertThat(response.emailVerified()).isFalse();
        assertThat(response.status()).isEqualTo(UserStatus.PENDING);
    }

    @Test
    @DisplayName("should map COMPANY role correctly")
    void shouldMapCompanyRoleCorrectly() {
        UUID userId = UUID.randomUUID();
        User companyUser = User.builder()
                .id(userId)
                .email("company@corp.com")
                .role(Role.COMPANY)
                .status(UserStatus.ACTIVE)
                .authProvider(AuthProvider.LOCAL)
                .emailVerified(true)
                .createdAt(LocalDateTime.now())
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(companyUser));

        UserResponse response = getCurrentUserUseCase.getCurrentUser(userId);

        assertThat(response.role()).isEqualTo(Role.COMPANY);
        assertThat(response.id()).isEqualTo(userId);
    }

    @Test
    @DisplayName("should map OAuth user (Google) correctly")
    void shouldMapOAuthUserCorrectly() {
        UUID userId = UUID.randomUUID();
        User oauthUser = User.builder()
                .id(userId)
                .email("oauth@gmail.com")
                .role(Role.STUDENT)
                .status(UserStatus.ACTIVE)
                .authProvider(AuthProvider.GOOGLE)
                .emailVerified(true)
                .createdAt(LocalDateTime.now())
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(oauthUser));

        UserResponse response = getCurrentUserUseCase.getCurrentUser(userId);

        assertThat(response.emailVerified()).isTrue();
        assertThat(response.status()).isEqualTo(UserStatus.ACTIVE);
    }
}