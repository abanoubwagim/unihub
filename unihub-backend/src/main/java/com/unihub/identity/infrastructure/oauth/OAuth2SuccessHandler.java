package com.unihub.identity.infrastructure.oauth;

import com.unihub.shared.security.JwtSubject;
import com.unihub.shared.security.service.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private static final String OAUTH2_CODE_PREFIX = "oauth2:code:";
    private static final Duration CODE_TTL = Duration.ofSeconds(60);
    private static final String OAUTH2_CALLBACK_PATH = "/oauth2/callback";
    private final JwtService jwtService;
    private final StringRedisTemplate redisTemplate;
    @Value("${app.frontend-url}")
    private String frontendUrl;

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

        try {
            var user = oAuth2User.getUser();
            String jwt = jwtService.generateToken(
                    new JwtSubject(user.getId(), user.getEmail(), user.getRole().name()));

            // Store JWT under a one-time code — never expose the JWT in the URL
            String code = UUID.randomUUID().toString();
            String redisKey = OAUTH2_CODE_PREFIX + code;
            redisTemplate.opsForValue().set(redisKey, jwt, CODE_TTL);

            String redirectUrl = UriComponentsBuilder
                    .fromUri(URI.create(frontendUrl))
                    .path(OAUTH2_CALLBACK_PATH)
                    .fragment("code=" + code)
                    .build()
                    .toUriString();

            log.info("OAuth2 success — redirecting userId={} via one-time code", user.getId());
            clearAuthenticationAttributes(request);
            getRedirectStrategy().sendRedirect(request, response, redirectUrl);

        } catch (Exception e) {
            log.error("OAuth2 success handler failed — error={}", e.getMessage(), e);

            String errorRedirect = UriComponentsBuilder
                    .fromUri(URI.create(frontendUrl))
                    .path(OAUTH2_CALLBACK_PATH)
                    .queryParam("error", "SERVER_ERROR")
                    .build()
                    .toUriString();

            getRedirectStrategy().sendRedirect(request, response, errorRedirect);
        }
    }
}