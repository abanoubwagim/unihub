package com.unihub.identity.application.impl;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.unihub.identity.api.dto.LoginRequest;
import com.unihub.identity.api.dto.LoginResponse;
import com.unihub.identity.application.usecase.LoginUserUseCase;
import com.unihub.identity.domain.enums.AuthProvider;
import com.unihub.identity.domain.enums.UserStatus;
import com.unihub.identity.domain.model.User;
import com.unihub.identity.domain.repository.UserRepository;
import com.unihub.shared.exception.UnauthorizedException;
import com.unihub.shared.security.JwtService;
import com.unihub.shared.security.JwtSubject;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LoginUserUseCaseImpl implements LoginUserUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Override
    public LoginResponse login(LoginRequest request) {


        // Get User
        String email = request.email().trim().toLowerCase();
        
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));


        if (user.getAuthProvider() != AuthProvider.LOCAL) {
            throw new UnauthorizedException(
                    "This account uses " + user.getAuthProvider().name().toLowerCase()
                    + " login. Please sign in with that provider.");
        }        

        // Check password
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid email or password");
        }

        // Check Email Verified
        if (!user.isEmailVerified()) {
            throw new UnauthorizedException("Email is not verified");
        }

        // Check status
        if (user.getStatus() == UserStatus.BANNED) {
            throw new UnauthorizedException("User is banned");
        }

        if (user.getStatus() == UserStatus.SUSPENDED) {
            throw new UnauthorizedException("User is suspended");
        }

        // Generate JWT
        String token = jwtService.generateToken(
                new JwtSubject(user.getId(), user.getEmail(), user.getRole().name()));

        // Return
        return new LoginResponse(token, "Bearer");
    }

}
