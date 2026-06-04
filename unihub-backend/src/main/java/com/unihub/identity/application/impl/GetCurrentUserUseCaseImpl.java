package com.unihub.identity.application.impl;

import com.unihub.identity.api.dto.res.UserResponse;
import com.unihub.identity.application.usecase.GetCurrentUserUseCase;
import com.unihub.identity.domain.repository.UserRepository;
import com.unihub.shared.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetCurrentUserUseCaseImpl implements GetCurrentUserUseCase {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
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