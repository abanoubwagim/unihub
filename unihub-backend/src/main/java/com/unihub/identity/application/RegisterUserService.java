package com.unihub.identity.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.unihub.identity.api.dto.RegisterReqeust;
import com.unihub.identity.api.dto.RegisterResponse;
import com.unihub.identity.application.event.EmailVerificationRequestedEvent;
import com.unihub.identity.domain.AuthProvider;
import com.unihub.identity.domain.User;
import com.unihub.identity.domain.UserRepository;
import com.unihub.identity.domain.UserStatus;
import com.unihub.shared.exception.BadRequestException;
import com.unihub.shared.exception.ConflictException;

import lombok.RequiredArgsConstructor;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;


@Service
@RequiredArgsConstructor
public class RegisterUserService  implements RegisterUserUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;  
    private final ApplicationEventPublisher eventPublisher;  
    
    
    @Override
    @Transactional
    public RegisterResponse register(RegisterReqeust request) {
       
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

        String otp = generateOtp();
        eventPublisher.publishEvent(new EmailVerificationRequestedEvent(user.getId(), user.getEmail(), otp));
        
        // Response
        return new RegisterResponse(
            user.getId(),
            user.getEmail(),
            user.getRole(),
            user.getStatus()
        );
    }

    private String generateOtp() {
        SecureRandom random = new SecureRandom();
        // Generate a 6-digit OTP
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }


}
