package com.unihub.identity.application.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.unihub.identity.api.dto.RegisterRequest;
import com.unihub.identity.api.dto.RegisterResponse;
import com.unihub.identity.application.event.EmailVerificationRequestedEvent;
import com.unihub.identity.application.usecase.RegisterUserUseCase;
import com.unihub.identity.domain.enums.AuthProvider;
import com.unihub.identity.domain.enums.UserStatus;
import com.unihub.identity.domain.event.UserRegisteredEvent;
import com.unihub.identity.domain.model.User;
import com.unihub.identity.domain.repository.UserRepository;
import com.unihub.shared.exception.BadRequestException;
import com.unihub.shared.exception.ConflictException;
import com.unihub.shared.util.OtpGenerator;

import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;


@Service
@RequiredArgsConstructor
public class RegisterUserUseCaseImpl  implements RegisterUserUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;  
    private final ApplicationEventPublisher eventPublisher;  
    
    
    @Override
    @Transactional
    public RegisterResponse register(RegisterRequest request) {
       
        String email = request.email().trim().toLowerCase();


        // Check if the email is already registered
        if (userRepository.existsByEmail(email)) {
            throw new ConflictException("Email is already registered"); 
        }

        // check password and confirm password
        if (!request.password().equals(request.confirmPassword())){
            throw new BadRequestException("Passwords do not match");
        }

        //Create a new user
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

        // Save
        userRepository.save(user);
        eventPublisher.publishEvent(new UserRegisteredEvent(user.getId(), user.getRole()));

        String otp = OtpGenerator.generate();
        eventPublisher.publishEvent(new EmailVerificationRequestedEvent(user.getId(), user.getEmail(), otp));
        
        // Response
        return new RegisterResponse(
            user.getId(),
            user.getEmail(),
            user.getRole(),
            user.getStatus()
        );
    }


}
