package com.unihub.identity.application.impl;

import com.unihub.identity.api.dto.RegisterRequest;
import com.unihub.identity.api.dto.RegisterResponse;
import com.unihub.identity.application.event.EmailVerificationRequestedEvent;
import com.unihub.identity.domain.enums.AuthProvider;
import com.unihub.identity.domain.enums.Role;
import com.unihub.identity.domain.enums.UserStatus;
import com.unihub.identity.domain.event.UserRegisteredEvent;
import com.unihub.identity.domain.model.User;
import com.unihub.identity.domain.repository.UserRepository;
import com.unihub.shared.exception.BadRequestException;
import com.unihub.shared.exception.ConflictException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RegisterUserUseCase Tests")
class RegisterUserUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private RegisterUserUseCaseImpl registerUserUseCase;

    private RegisterRequest validRequest() {
        return new RegisterRequest(
                "student@example.com",
                "Password1@",
                "Password1@",
                Role.STUDENT);
    }

    @Test
    @DisplayName("should register successfully and return response with correct fields")
    void shouldRegisterSuccessfully() {
        when(userRepository.existsByEmail("student@example.com")).thenReturn(false);
        when(passwordEncoder.encode("Password1@")).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        RegisterResponse response = registerUserUseCase.register(validRequest());

        assertThat(response).isNotNull();
        assertThat(response.email()).isEqualTo("student@example.com");
        assertThat(response.role()).isEqualTo(Role.STUDENT);
        assertThat(response.status()).isEqualTo(UserStatus.PENDING);
        assertThat(response.userId()).isNotNull();

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getAuthProvider()).isEqualTo(AuthProvider.LOCAL);
        assertThat(savedUser.isEmailVerified()).isFalse();
        assertThat(savedUser.getStatus()).isEqualTo(UserStatus.PENDING);
    }

    @Test
    @DisplayName("should trim and lowercase the email before saving")
    void shouldNormalizeEmail() {
        when(userRepository.existsByEmail("student@example.com")).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        RegisterRequest requestWithMessyEmail = new RegisterRequest(
                "  STUDENT@Example.COM  ",
                "Password1@",
                "Password1@",
                Role.STUDENT);

        RegisterResponse response = registerUserUseCase.register(requestWithMessyEmail);

        assertThat(response.email()).isEqualTo("student@example.com");
    }

    @Test
    @DisplayName("should publish UserRegisteredEvent and EmailVerificationRequestedEvent with correct payload")
    void shouldPublishBothEventsWithCorrectPayload() {
        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        registerUserUseCase.register(validRequest());

        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(eventPublisher, times(2)).publishEvent(captor.capture());

        assertThat(captor.getAllValues())
                .hasAtLeastOneElementOfType(UserRegisteredEvent.class)
                .hasAtLeastOneElementOfType(EmailVerificationRequestedEvent.class);

        UserRegisteredEvent registeredEvent = captor.getAllValues().stream()
                .filter(e -> e instanceof UserRegisteredEvent)
                .map(e -> (UserRegisteredEvent) e)
                .findFirst().orElseThrow();

        assertThat(registeredEvent.role()).isEqualTo(Role.STUDENT);
        assertThat(registeredEvent.userId()).isNotNull();

        EmailVerificationRequestedEvent verificationEvent = captor.getAllValues().stream()
                .filter(e -> e instanceof EmailVerificationRequestedEvent)
                .map(e -> (EmailVerificationRequestedEvent) e)
                .findFirst().orElseThrow();

        assertThat(verificationEvent.email()).isEqualTo("student@example.com");
        assertThat(verificationEvent.otp()).isNotBlank();
        assertThat(verificationEvent.userId()).isEqualTo(registeredEvent.userId());
    }

    @Test
    @DisplayName("should encode the password before saving")
    void shouldEncodePassword() {
        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(passwordEncoder.encode("Password1@")).thenReturn("bcrypt_hashed");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        registerUserUseCase.register(validRequest());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        assertThat(userCaptor.getValue().getPasswordHash()).isEqualTo("bcrypt_hashed");
        assertThat(userCaptor.getValue().getPasswordHash()).doesNotContain("Password1@");
    }

    @Test
    @DisplayName("should throw ConflictException when email already exists")
    void shouldThrowWhenEmailAlreadyExists() {
        when(userRepository.existsByEmail("student@example.com")).thenReturn(true);

        assertThatThrownBy(() -> registerUserUseCase.register(validRequest()))
                .isInstanceOf(ConflictException.class);

        verify(userRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("should throw BadRequestException when passwords do not match")
    void shouldThrowWhenPasswordsDoNotMatch() {
        when(userRepository.existsByEmail(any())).thenReturn(false);

        RegisterRequest mismatchRequest = new RegisterRequest(
                "student@example.com",
                "Password1@",
                "DifferentPass2@",
                Role.STUDENT);

        assertThatThrownBy(() -> registerUserUseCase.register(mismatchRequest))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("match");

        verify(userRepository, never()).save(any());
    }
}