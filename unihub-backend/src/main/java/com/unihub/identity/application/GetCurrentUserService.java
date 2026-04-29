package com.unihub.identity.application;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.unihub.identity.api.dto.UserResponse;
import com.unihub.identity.domain.UserRepository;
import com.unihub.shared.exception.UnauthorizedException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetCurrentUserService implements GetCurrentUserUseCase {

    private final UserRepository userRepository;

    @Override
    public UserResponse getCurrentUser(UUID userId) {
        return userRepository.findById(userId)
                .map(user -> new UserResponse(
                        user.getId(),
                        user.getEmail(),
                        user.getRole(),
                        user.getStatus(),
                        user.isEmailVerified()))
                .orElseThrow(() -> new UnauthorizedException("User not found"));
    }
}