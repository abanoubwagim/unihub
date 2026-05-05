package com.unihub.identity.infrastructure.oauth;

import com.unihub.identity.domain.enums.AuthProvider;
import com.unihub.identity.domain.enums.Role;
import com.unihub.identity.domain.enums.UserStatus;
import com.unihub.identity.domain.event.UserRegisteredEvent;
import com.unihub.identity.domain.model.User;
import com.unihub.identity.domain.repository.UserRepository;
import com.unihub.shared.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UniHubOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        // Load raw user info from provider 
        OAuth2User oAuth2User = super.loadUser(userRequest);

        // Determine which provider this is
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        AuthProvider provider = resolveProvider(registrationId);

        // Extract unified user info using the factory
        OAuth2UserInfo userInfo = OAuth2UserInfoFactory.create(registrationId, oAuth2User.getAttributes());

        String email = userInfo.getEmail();
        if (email == null || email.isBlank()) {
            throw new OAuth2AuthenticationException(
                    "Email not returned by OAuth2 provider: " + registrationId
                            + ". Please ensure your provider account has a verified email.");
        }

        String normalizedEmail = email.trim().toLowerCase();

        // Find-or-create
        User user = userRepository.findByEmail(normalizedEmail)
                .map(existing -> handleExistingUser(existing, provider, userInfo))
                .orElseGet(() -> createNewOAuthUser(normalizedEmail, provider, userInfo));

        log.info("OAuth2 login — provider={}, email={}, userId={}", provider, normalizedEmail, user.getId());

        return new UniHubOAuth2User(user, oAuth2User.getAttributes());
    }



    private User handleExistingUser(User existing, AuthProvider incomingProvider, OAuth2UserInfo userInfo) {

        // Case: email already registered with LOCAL (password) account
        if (existing.getAuthProvider() == AuthProvider.LOCAL) {
            throw new OAuth2AuthenticationException(
                    "EMAIL_REGISTERED_WITH_PASSWORD|"
                            + "An account with this email already exists. "
                            + "Please login with your email and password.");
        }

        // Case: email registered with a DIFFERENT OAuth provider
        if (existing.getAuthProvider() != incomingProvider) {
            throw new OAuth2AuthenticationException(
                    "PROVIDER_MISMATCH|"
                            + "This account is linked to "
                            + existing.getAuthProvider()
                            + ". Please login with that provider.");
        }

        // Case: same provider → normal login, no changes needed
        if (existing.getStatus() == UserStatus.BANNED) {
            throw new OAuth2AuthenticationException("ACCOUNT_BANNED|Your account has been banned.");
        }
        if (existing.getStatus() == UserStatus.SUSPENDED) {
            throw new OAuth2AuthenticationException("ACCOUNT_SUSPENDED|Your account has been suspended.");
        }

        return existing;
    }

    private User createNewOAuthUser(String email, AuthProvider provider, OAuth2UserInfo userInfo) {
        LocalDateTime now = LocalDateTime.now();

        User newUser = User.builder()
                .id(UUID.randomUUID())
                .email(email)
                .passwordHash(null) // OAuth users have no password
                .role(Role.STUDENT) // Default role for OAuth sign-ups
                .status(UserStatus.ACTIVE) // OAuth users are auto-verified (email proven by provider)
                .authProvider(provider)
                .emailVerified(true) // Provider already verified the email
                .createdAt(now)
                .updatedAt(now)
                .build();

        User saved = userRepository.save(newUser);
        eventPublisher.publishEvent(new UserRegisteredEvent(saved.getId(), saved.getRole()));

        log.info("Created new OAuth user — provider={}, email={}, userId={}", provider, email, saved.getId());
        return saved;
    }

    private AuthProvider resolveProvider(String registrationId) {
        return switch (registrationId.toLowerCase()) {
            case "google" -> AuthProvider.GOOGLE;
            case "microsoft" -> AuthProvider.MICROSOFT;
            default -> throw new BadRequestException(
                    "Unsupported OAuth2 provider: " + registrationId);
        };
    }
}