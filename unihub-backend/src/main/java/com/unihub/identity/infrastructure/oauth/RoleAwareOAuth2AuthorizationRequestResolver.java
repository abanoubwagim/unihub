package com.unihub.identity.infrastructure.oauth;

import com.unihub.identity.domain.enums.Role;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;


@Slf4j
public class RoleAwareOAuth2AuthorizationRequestResolver
        implements OAuth2AuthorizationRequestResolver {

    // Session key under which the requested role name is stored.
    public static final String SESSION_ROLE_KEY = "oauth2_requested_role";

    private final OAuth2AuthorizationRequestResolver delegate;

    public RoleAwareOAuth2AuthorizationRequestResolver(
            ClientRegistrationRepository clientRegistrationRepository,
            String authorizationRequestBaseUri) {
        this.delegate = new DefaultOAuth2AuthorizationRequestResolver(
                clientRegistrationRepository, authorizationRequestBaseUri);
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
        storeRoleInSession(request);
        return delegate.resolve(request);
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request,
                                              String clientRegistrationId) {
        storeRoleInSession(request);
        return delegate.resolve(request, clientRegistrationId);
    }


    private void storeRoleInSession(HttpServletRequest request) {
        String roleParam = request.getParameter("role");
        if (roleParam == null || roleParam.isBlank()) {
            return;
        }

        Role role = parseRole(roleParam);
        HttpSession session = request.getSession();
        session.setAttribute(SESSION_ROLE_KEY, role.name());
        log.debug("OAuth2 requested role stored in session — role={}", role);
    }

    private Role parseRole(String roleParam) {
        try {
            Role role = Role.valueOf(roleParam.trim().toUpperCase());
            if (role == Role.ADMIN) {
                log.warn("OAuth2 role request for ADMIN rejected — defaulting to STUDENT ");
                return Role.STUDENT;
            }
            return role;
        } catch (IllegalArgumentException e) {
            log.warn("Unknown OAuth2 role param '{}' — defaulting to STUDENT", roleParam);
            return Role.STUDENT;
        }
    }
}