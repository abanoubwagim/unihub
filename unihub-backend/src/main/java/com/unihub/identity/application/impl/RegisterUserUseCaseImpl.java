package com.unihub.identity.application.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.unihub.identity.api.dto.RegisterRequest;
import com.unihub.identity.api.dto.RegisterResponse;
import com.unihub.identity.application.event.EmailVerificationRequestedEvent;
import com.unihub.identity.application.usecase.RegisterUserUseCase;
import com.unihub.identity.domain.config.IdentityConstants;
import com.unihub.identity.domain.enums.AuthProvider;
import com.unihub.identity.domain.enums.Role;
import com.unihub.identity.domain.enums.UserStatus;
import com.unihub.identity.domain.event.UserRegisteredEvent;
import com.unihub.identity.domain.model.EmailVerificationToken;
import com.unihub.identity.domain.model.User;
import com.unihub.identity.domain.repository.EmailVerificationTokenRepository;
import com.unihub.identity.domain.repository.UserRepository;
import com.unihub.shared.config.RabbitMqConfig;
import com.unihub.shared.exception.BadRequestException;
import com.unihub.shared.exception.ConflictException;
import com.unihub.shared.outbox.OutboxMessage;
import com.unihub.shared.outbox.OutboxMessageRepository;
import com.unihub.shared.util.OtpGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RegisterUserUseCaseImpl implements RegisterUserUseCase {

    private final UserRepository userRepository;
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher eventPublisher;
    private final OutboxMessageRepository outboxMessageRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        String email = request.email().trim().toLowerCase();

        if (request.role() == Role.ADMIN) {
            throw new BadRequestException("Cannot register as ADMIN");
        }
        if (!request.password().equals(request.confirmPassword())) {
            throw new BadRequestException("Passwords do not match");
        }
        if (userRepository.existsByEmail(email)) {
            throw new ConflictException("Email is already registered");
        }

        User user = User.builder()
                .id(UUID.randomUUID())
                .email(email)
                .passwordHash(passwordEncoder.encode(request.password()))
                .role(request.role())
                .status(UserStatus.PENDING)
                .authProvider(AuthProvider.LOCAL)
                .emailVerified(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        try {
            userRepository.save(user);
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            log.warn("Concurrent registration conflict — email={}", email);
            throw new ConflictException("Email is already registered");
        }

        try {
            UserRegisteredEvent event = new UserRegisteredEvent(user.getId(), user.getRole());
            String payload = objectMapper.writeValueAsString(event);

            OutboxMessage outboxMessage = OutboxMessage.builder()
                    .id(UUID.randomUUID())
                    .exchange(RabbitMqConfig.USER_REGISTERED_EXCHANGE)
                    .routingKey("")
                    .payload(payload)
                    .payloadType(UserRegisteredEvent.class.getName())
                    .createdAt(LocalDateTime.now())
                    .attempts(0)
                    .build();

            outboxMessageRepository.save(outboxMessage);

        } catch (JsonProcessingException e) {
            // This should never happen — UserRegisteredEvent is a simple record
            throw new IllegalStateException(
                    "Failed to serialize UserRegisteredEvent — userId=" + user.getId(), e);
        }

        String otp = OtpGenerator.generate();
        String otpHash = passwordEncoder.encode(otp);

        EmailVerificationToken token = EmailVerificationToken.builder()
                .id(UUID.randomUUID())
                .userId(user.getId())
                .otpHash(otpHash)
                .expiresAt(LocalDateTime.now().plusMinutes(IdentityConstants.OTP_EXPIRY_MINUTES))
                .used(false)
                .attempts(0)
                .createdAt(LocalDateTime.now())
                .build();

        emailVerificationTokenRepository.save(token);

        eventPublisher.publishEvent(
                new EmailVerificationRequestedEvent(user.getId(), user.getEmail(), otp));

        log.info("User registered — userId={}, role={}", user.getId(), user.getRole());

        return new RegisterResponse(user.getId(), user.getEmail(), user.getRole(), user.getStatus());
    }
}