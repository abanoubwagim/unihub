package com.unihub.identity.infrastructure.oauth;

import com.unihub.identity.domain.model.User;
import com.unihub.shared.security.JwtService;
import com.unihub.shared.security.JwtSubject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;


@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtService jwtService;

    @Value("${app.frontend-url:http://localhost:4200}")
    private String frontendUrl;


    private static final String OAUTH2_CALLBACK_PATH = "/oauth2/callback";

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException {

        if (!(authentication.getPrincipal() instanceof UniHubOAuth2User oAuth2User)) {
            log.error("OAuth2SuccessHandler received unexpected principal type: {}",
                    authentication.getPrincipal().getClass());
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }

        User user = oAuth2User.getUser();

        String token = jwtService.generateToken(
                new JwtSubject(user.getId(), user.getEmail(), user.getRole().name()));

        String redirectUrl = buildRedirectUrl(token);
        log.info("OAuth2 success — redirecting userId={} to frontend", user.getId());

        clearAuthenticationAttributes(request);

        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }

    private String buildRedirectUrl(String token) {
        return UriComponentsBuilder
                .fromUri(URI.create(frontendUrl + OAUTH2_CALLBACK_PATH))
                .queryParam("token", token)
                .queryParam("tokenType", "Bearer")
                .build()
                .toUriString();
    }
}