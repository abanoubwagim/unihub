package com.unihub.identity.application.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class LoginUserUseCaseImpl implements LoginUserUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Override
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {


        // Get User
        String email = request.email().trim().toLowerCase();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("Login failed — email not found");
                    return new UnauthorizedException("Invalid email or password");
                });

        if (user.getAuthProvider() != AuthProvider.LOCAL) {
            log.warn("Login failed — OAuth account attempted password login: userId={}, provider={}",
                    user.getId(), user.getAuthProvider());
            throw new UnauthorizedException("Invalid email or password");
        }

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            log.warn("Login failed — wrong password: userId={}", user.getId());
            throw new UnauthorizedException("Invalid email or password");
        }

        if (!user.isEmailVerified()) {
            log.warn("Login failed — email not verified: userId={}", user.getId());
            throw new UnauthorizedException("Email is not verified");
        }

        if (user.getStatus() == UserStatus.BANNED) {
            log.warn("Login failed — account banned: userId={}", user.getId());
            throw new UnauthorizedException("User is banned");
        }

        if (user.getStatus() == UserStatus.SUSPENDED) {
            log.warn("Login failed — account suspended: userId={}", user.getId());
            throw new UnauthorizedException("User is suspended");
        }

        String token = jwtService.generateToken(
                new JwtSubject(user.getId(), user.getEmail(), user.getRole().name()));

        log.info("Login successful — userId={}, role={}", user.getId(), user.getRole());
        return new LoginResponse(token, "Bearer");
    }
}