package com.unihub.identity.application.impl;

import com.unihub.identity.application.usecase.DeleteAccountUseCase;
import com.unihub.identity.domain.enums.AuthProvider;
import com.unihub.identity.domain.model.User;
import com.unihub.identity.domain.repository.UserRepository;
import com.unihub.shared.exception.BadRequestException;
import com.unihub.shared.exception.NotFoundException;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeleteAccountUseCaseImpl implements DeleteAccountUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void deleteAccount(UUID userId, String password) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (user.getAuthProvider() != AuthProvider.LOCAL) {
            throw new BadRequestException(
                    "OAuth accounts (" + user.getAuthProvider().name().toLowerCase() +
                            ") cannot be deleted via this endpoint. " +
                            "Please revoke access from your OAuth provider settings.");
        }

        // LOCAL accounts
        if (password == null || password.isBlank()) {
            throw new BadRequestException("Password is required to delete account");
        }
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new BadRequestException("Incorrect password");
        }

        userRepository.deleteById(userId);
    }
}