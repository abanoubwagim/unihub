package com.unihub.identity.application;

import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.unihub.identity.domain.AuthProvider;
import com.unihub.identity.domain.User;
import com.unihub.identity.domain.UserRepository;
import com.unihub.shared.exception.BadRequestException;
import com.unihub.shared.exception.NotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DeleteAccountService implements DeleteAccountUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void deleteAccount(UUID userId, String password) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (user.getAuthProvider() != AuthProvider.LOCAL) {
            throw new BadRequestException("OAuth accounts cannot be deleted this way");
        }

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new BadRequestException("Incorrect password");
        }

        userRepository.deleteById(userId);
    }
}
