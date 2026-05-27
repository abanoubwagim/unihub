package com.unihub.identity.application.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.unihub.identity.api.dto.RegisterRequest;
import com.unihub.identity.api.dto.RegisterResponse;
import com.unihub.identity.application.event.EmailVerificationRequestedEvent;
import com.unihub.identity.domain.enums.AuthProvider;
import com.unihub.identity.domain.enums.Role;
import com.unihub.identity.domain.enums.UserStatus;
import com.unihub.identity.domain.model.User;
import com.unihub.identity.domain.repository.EmailVerificationTokenRepository;
import com.unihub.identity.domain.repository.UserRepository;
import com.unihub.shared.exception.BadRequestException;
import com.unihub.shared.exception.ConflictException;
import com.unihub.shared.outbox.OutboxMessageRepository;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
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

    @Mock
    private EmailVerificationTokenRepository emailVerificationTokenRepository;

    @Mock
    private OutboxMessageRepository outboxMessageRepository;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private RegisterUserUseCaseImpl registerUserUseCase;

    private RegisterRequest validRequest() {
        return new RegisterRequest(
                "student@example.com",
                "Password1@",
                "Password1@",
                Role.STUDENT);
    }

    private void stubHappyPath() throws JsonProcessingException {
        when(userRepository.existsByEmail("student@example.com")).thenReturn(false);
        when(passwordEncoder.encode("Password1@")).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            org.springframework.test.util.ReflectionTestUtils.setField(u, "id", UUID.randomUUID());
            return u;
        });
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"userId\":\"x\",\"role\":\"STUDENT\"}");
    }

    @Test
    @DisplayName("should register successfully and return response with correct fields")
    void shouldRegisterSuccessfully() throws JsonProcessingException {
        stubHappyPath();

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
    void shouldNormalizeEmail() throws JsonProcessingException {
        when(userRepository.existsByEmail("student@example.com")).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        RegisterRequest requestWithMessyEmail = new RegisterRequest(
                "  STUDENT@Example.COM  ", "Password1@", "Password1@", Role.STUDENT);

        RegisterResponse response = registerUserUseCase.register(requestWithMessyEmail);

        assertThat(response.email()).isEqualTo("student@example.com");
    }

    @Test
    @DisplayName("should publish EmailVerificationRequestedEvent after successful registration")
    void shouldPublishVerificationEventWithCorrectPayload() throws JsonProcessingException {
        stubHappyPath();

        registerUserUseCase.register(validRequest());

        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(eventPublisher, times(1)).publishEvent(captor.capture());

        assertThat(captor.getValue()).isInstanceOf(EmailVerificationRequestedEvent.class);

        EmailVerificationRequestedEvent verificationEvent =
                (EmailVerificationRequestedEvent) captor.getValue();
        assertThat(verificationEvent.email()).isEqualTo("student@example.com");
        assertThat(verificationEvent.otp()).isNotBlank();
        assertThat(verificationEvent.userId()).isNotNull();
    }

    @Test
    @DisplayName("should save UserRegisteredEvent to the outbox (not via eventPublisher)")
    void shouldSaveUserRegisteredEventToOutbox() throws JsonProcessingException {
        stubHappyPath();

        registerUserUseCase.register(validRequest());

        verify(outboxMessageRepository).save(any());
    }

    @Test
    @DisplayName("should save EmailVerificationToken with correct initial state")
    void shouldSaveEmailVerificationToken() throws JsonProcessingException {
        stubHappyPath();

        registerUserUseCase.register(validRequest());

        verify(emailVerificationTokenRepository).save(argThat(token ->
                !token.isUsed()
                        && token.getAttempts() == 0
                        && token.getExpiresAt() != null
                        && token.getExpiresAt().isAfter(LocalDateTime.now())
        ));
    }

    @Test
    @DisplayName("should encode the password before saving")
    void shouldEncodePassword() throws JsonProcessingException {
        stubHappyPath();

        registerUserUseCase.register(validRequest());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        assertThat(userCaptor.getValue().getPasswordHash()).isEqualTo("hashed");
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
                "student@example.com", "Password1@", "DifferentPass2@", Role.STUDENT);

        assertThatThrownBy(() -> registerUserUseCase.register(mismatchRequest))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("match");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("should throw BadRequestException when role is ADMIN")
    void shouldThrowWhenRoleIsAdmin() {
        RegisterRequest adminRequest = new RegisterRequest(
                "admin@example.com", "Password1@", "Password1@", Role.ADMIN);

        assertThatThrownBy(() -> registerUserUseCase.register(adminRequest))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("ADMIN");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("should publish a 6-digit OTP in the EmailVerificationRequestedEvent")
    void shouldPublishSixDigitOtp() throws JsonProcessingException {
        stubHappyPath();

        registerUserUseCase.register(validRequest());

        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(eventPublisher).publishEvent(captor.capture());
        EmailVerificationRequestedEvent event = (EmailVerificationRequestedEvent) captor.getValue();
        assertThat(event.otp()).matches("\\d{6}");
    }
}