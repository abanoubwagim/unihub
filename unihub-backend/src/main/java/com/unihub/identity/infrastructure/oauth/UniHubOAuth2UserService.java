package com.unihub.identity.infrastructure.oauth;

import com.unihub.identity.domain.enums.AuthProvider;
import com.unihub.identity.domain.enums.Role;
import com.unihub.identity.domain.model.User;
import com.unihub.shared.exception.BadRequestException;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j
@Service
@RequiredArgsConstructor
public class UniHubOAuth2UserService extends DefaultOAuth2UserService {

    private final OAuthUserPersistenceService persistenceService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        OAuth2User oAuth2User = super.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        AuthProvider provider = resolveProvider(registrationId);
        OAuth2UserInfo userInfo = OAuth2UserInfoFactory.create(registrationId, oAuth2User.getAttributes());

        String email = userInfo.getEmail();
        if (email == null || email.isBlank()) {
            throw new OAuth2AuthenticationException(
                    "Email not returned by OAuth2 provider: " + registrationId
                            + ". Please ensure your provider account has a verified email.");
        }

        String normalizedEmail = email.trim().toLowerCase();

        Role requestedRole = resolveRequestedRoleFromSession();

        User user = persistenceService.findOrCreateUser(normalizedEmail, provider, requestedRole);

        log.info("OAuth2 login — provider={}, role={}, userId={}",
                provider, user.getRole(), user.getId());

        return new UniHubOAuth2User(user, oAuth2User.getAttributes());
    }

    private Role resolveRequestedRoleFromSession() {
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs == null) {
                return Role.STUDENT;
            }

            HttpSession session = attrs.getRequest().getSession(false);
            if (session == null) {
                return Role.STUDENT;
            }

            String roleStr = (String) session.getAttribute(
                    RoleAwareOAuth2AuthorizationRequestResolver.SESSION_ROLE_KEY);

            if (roleStr == null || roleStr.isBlank()) {
                return Role.STUDENT;
            }

            Role role;
            try {
                role = Role.valueOf(roleStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                log.warn("OAuth2 session contained unrecognised role='{}' — defaulting to STUDENT",
                        roleStr);
                return Role.STUDENT;
            }

            if (role == Role.ADMIN) {
                log.warn("OAuth2 session contained ADMIN role — potential privilege escalation attempt. "
                        + "Defaulting to STUDENT.");
                session.removeAttribute(
                        RoleAwareOAuth2AuthorizationRequestResolver.SESSION_ROLE_KEY);
                return Role.STUDENT;
            }

            session.removeAttribute(RoleAwareOAuth2AuthorizationRequestResolver.SESSION_ROLE_KEY);
            log.debug("OAuth2 requested role resolved from session — role={}", role);
            return role;

        } catch (Exception e) {
            log.warn("Could not resolve OAuth2 requested role from session — defaulting to STUDENT: {}",
                    e.getMessage());
            return Role.STUDENT;
        }
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