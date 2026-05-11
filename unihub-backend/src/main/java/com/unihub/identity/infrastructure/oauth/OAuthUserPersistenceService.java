package com.unihub.identity.infrastructure.oauth;

import com.unihub.identity.domain.enums.AuthProvider;
import com.unihub.identity.domain.enums.Role;
import com.unihub.identity.domain.enums.UserStatus;
import com.unihub.identity.domain.model.User;
import com.unihub.identity.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OAuthUserPersistenceService {

    private final UserRepository userRepository;
    private final OAuthUserCreator userCreator;

    @Transactional
    public User findOrCreateUser(String normalizedEmail,
                                 AuthProvider provider,
                                 OAuth2UserInfo userInfo,
                                 Role requestedRole) {
        return userRepository.findByEmail(normalizedEmail)
                .map(existing -> handleExistingUser(existing, provider))
                .orElseGet(() -> createNewOAuthUser(normalizedEmail, provider, requestedRole));
    }


    private User handleExistingUser(User existing, AuthProvider incomingProvider) {
        if (existing.getAuthProvider() == AuthProvider.LOCAL) {
            throw new OAuth2AuthenticationException(
                    "EMAIL_REGISTERED_WITH_PASSWORD|"
                    + "An account with this email already exists. "
                    + "Please login with your email and password.");
        }
        if (existing.getAuthProvider() != incomingProvider) {
            throw new OAuth2AuthenticationException(
                    "PROVIDER_MISMATCH|This account is linked to "
                    + existing.getAuthProvider()
                    + ". Please login with that provider.");
        }
        if (existing.getStatus() == UserStatus.BANNED) {
            throw new OAuth2AuthenticationException(
                    "ACCOUNT_BANNED|Your account has been banned.");
        }
        if (existing.getStatus() == UserStatus.SUSPENDED) {
            throw new OAuth2AuthenticationException(
                    "ACCOUNT_SUSPENDED|Your account has been suspended.");
        }
        return existing;
    }

    private User createNewOAuthUser(String email, AuthProvider provider, Role role) {
        LocalDateTime now = LocalDateTime.now();
        User newUser = User.builder()
                .id(UUID.randomUUID())
                .email(email)
                .passwordHash(null)
                .role(role)             
                .status(UserStatus.ACTIVE)
                .authProvider(provider)
                .emailVerified(true)
                .createdAt(now)
                .updatedAt(now)
                .build();

        try {
            // REQUIRES_NEW — if this throws, only its own transaction rolls back.
            return userCreator.tryCreate(newUser);
        } catch (DataIntegrityViolationException e) {
            // Concurrent OAuth registration race — fetch the winner's record
            log.warn("Concurrent OAuth registration — fetching existing record — email hash={}",
                    email.hashCode());
            return userRepository.findByEmail(email)
                    .orElseThrow(() -> new OAuth2AuthenticationException(
                            "SERVER_ERROR|Failed to process OAuth2 login. Please try again."));
        }
    }
}