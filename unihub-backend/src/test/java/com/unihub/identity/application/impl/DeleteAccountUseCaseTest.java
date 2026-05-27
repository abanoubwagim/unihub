package com.unihub.identity.application.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.unihub.identity.domain.enums.AuthProvider;
import com.unihub.identity.domain.enums.Role;
import com.unihub.identity.domain.enums.UserStatus;
import com.unihub.identity.domain.model.User;
import com.unihub.identity.domain.repository.EmailVerificationTokenRepository;
import com.unihub.identity.domain.repository.PasswordResetTokenRepository;
import com.unihub.identity.domain.repository.UserRepository;
import com.unihub.shared.events.UserDeletedEvent;
import com.unihub.shared.exception.BadRequestException;
import com.unihub.shared.exception.NotFoundException;
import com.unihub.shared.outbox.OutboxMessageRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DeleteAccountUseCase Tests")
class DeleteAccountUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailVerificationTokenRepository emailVerificationTokenRepository;

    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Mock
    private OutboxMessageRepository outboxMessageRepository;

    @Mock
    private ObjectMapper objectMapper;
    
    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private DeleteAccountUseCaseImpl deleteAccountUseCase;

    private User buildLocalUser(UUID id) {
        return User.builder()
                .id(id).email("user@example.com")
                .passwordHash("hashed").role(Role.STUDENT)
                .status(UserStatus.ACTIVE).authProvider(AuthProvider.LOCAL)
                .emailVerified(true).createdAt(LocalDateTime.now()).build();
    }

    @Test
    @DisplayName("should delete account when password matches")
    void shouldDeleteAccountSuccessfully() throws JsonProcessingException {

        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.of(buildLocalUser(userId)));
        when(passwordEncoder.matches("correct_pass", "hashed")).thenReturn(true);
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"userId\":\"" + userId + "\"}");

        assertThatNoException().isThrownBy(
                () -> deleteAccountUseCase.deleteAccount(userId, "correct_pass"));

        verify(passwordEncoder).matches("correct_pass", "hashed");
        verify(emailVerificationTokenRepository).deleteByUserId(userId);
        verify(passwordResetTokenRepository).deleteByUserId(userId);
        verify(userRepository).deleteById(userId);
        verify(outboxMessageRepository).save(any());
        // FIX: verify event is published after account deletion
        verify(eventPublisher).publishEvent(any(UserDeletedEvent.class));
    }

    @Test
    @DisplayName("should throw NotFoundException when user does not exist")
    void shouldThrowWhenUserNotFound() {

        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> deleteAccountUseCase.deleteAccount(userId, "any_pass"))
                .isInstanceOf(NotFoundException.class);

        verify(userRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("should throw BadRequestException when password is wrong")
    void shouldThrowWhenPasswordIsWrong() {

        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.of(buildLocalUser(userId)));
        when(passwordEncoder.matches("wrong_pass", "hashed")).thenReturn(false);

        assertThatThrownBy(() -> deleteAccountUseCase.deleteAccount(userId, "wrong_pass"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Incorrect password");

        verify(userRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("should throw BadRequestException for OAuth accounts")
    void shouldThrowForOAuthAccount() {
        UUID userId = UUID.randomUUID();
        User oauthUser = User.builder()
                .id(userId).email("user@google.com")
                .role(Role.STUDENT).status(UserStatus.ACTIVE)
                .authProvider(AuthProvider.GOOGLE)
                .emailVerified(true).createdAt(LocalDateTime.now()).build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(oauthUser));

        assertThatThrownBy(() -> deleteAccountUseCase.deleteAccount(userId, "any"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("OAuth");

        // OAuth check comes before password check — encoder must never be called
        verify(passwordEncoder, never()).matches(any(), any());
        verify(userRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("should throw BadRequestException when password is blank")
    void shouldThrowWhenPasswordIsBlank() {

        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.of(buildLocalUser(userId)));

        assertThatThrownBy(() -> deleteAccountUseCase.deleteAccount(userId, "   "))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Password is required");

        verify(passwordEncoder, never()).matches(any(), any());
        verify(userRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("should throw BadRequestException when password is null")
    void shouldThrowWhenPasswordIsNull() {

        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.of(buildLocalUser(userId)));

        assertThatThrownBy(() -> deleteAccountUseCase.deleteAccount(userId, null))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Password is required");

        verify(passwordEncoder, never()).matches(any(), any());
        verify(userRepository, never()).deleteById(any());
    }
}