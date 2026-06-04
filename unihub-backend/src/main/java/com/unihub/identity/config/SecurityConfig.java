package com.unihub.identity.config;

import com.unihub.identity.infrastructure.oauth.OAuth2FailureHandler;
import com.unihub.identity.infrastructure.oauth.OAuth2SuccessHandler;
import com.unihub.identity.infrastructure.oauth.RoleAwareOAuth2AuthorizationRequestResolver;
import com.unihub.identity.infrastructure.oauth.UniHubOAuth2UserService;
import com.unihub.shared.security.JwtAuthenticationFilter;
import com.unihub.shared.security.LoginRateLimitFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private static final String OAUTH2_BASE_URI = "/oauth2/authorize";

    private final JwtAuthenticationFilter jwtFilter;
    private final UniHubOAuth2UserService oAuth2UserService;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final OAuth2FailureHandler oAuth2FailureHandler;
    private final LoginRateLimitFilter loginRateLimitFilter;
    private final ClientRegistrationRepository clientRegistrationRepository;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    public SecurityConfig(JwtAuthenticationFilter jwtFilter,
                          UniHubOAuth2UserService oAuth2UserService,
                          OAuth2SuccessHandler oAuth2SuccessHandler,
                          OAuth2FailureHandler oAuth2FailureHandler,
                          LoginRateLimitFilter loginRateLimitFilter,
                          ClientRegistrationRepository clientRegistrationRepository) {
        this.jwtFilter = jwtFilter;
        this.oAuth2UserService = oAuth2UserService;
        this.oAuth2SuccessHandler = oAuth2SuccessHandler;
        this.oAuth2FailureHandler = oAuth2FailureHandler;
        this.loginRateLimitFilter = loginRateLimitFilter;
        this.clientRegistrationRepository = clientRegistrationRepository;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth

                        // Infrastructure / Public assets
                        .requestMatchers("/actuator/**").permitAll()
                        .requestMatchers("/favicon.ico").permitAll()
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**").permitAll()

                        // WebSocket
                        .requestMatchers("/ws/**").permitAll()

                        // Public reference data
                        .requestMatchers("/api/v1/metadata/**").permitAll()

                        // Auth module
                        .requestMatchers("/api/v1/auth/me").authenticated()
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        .requestMatchers("/oauth2/**", "/login/oauth2/**").permitAll()

                        // Account module
                        .requestMatchers("/api/v1/account/**").authenticated()

                        // Student module
                        .requestMatchers("/api/v1/students/me/**").hasRole("STUDENT")
                        .requestMatchers(HttpMethod.GET, "/api/v1/students/**").authenticated()
                        .requestMatchers("/api/v1/students/**").hasRole("STUDENT")

                        // University module
                        .requestMatchers(HttpMethod.GET, "/api/v1/universities").permitAll()
                        .requestMatchers("/api/v1/universities/me/**").hasRole("UNIVERSITY")

                        // Company module
                        .requestMatchers("/api/v1/companies/**").hasRole("COMPANY")

                        // Notifications module
                        .requestMatchers("/api/v1/notifications/**").authenticated()

                        // Chat module
                        .requestMatchers("/api/v1/chat/**").authenticated()
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .authorizationEndpoint(endpoint -> endpoint
                                .baseUri(OAUTH2_BASE_URI)
                                .authorizationRequestResolver(
                                        new RoleAwareOAuth2AuthorizationRequestResolver(
                                                clientRegistrationRepository, OAUTH2_BASE_URI)))
                        .redirectionEndpoint(endpoint -> endpoint
                                .baseUri("/login/oauth2/code/*"))
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(oAuth2UserService))
                        .successHandler(oAuth2SuccessHandler)
                        .failureHandler(oAuth2FailureHandler))
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(loginRateLimitFilter, JwtAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(frontendUrl));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept"));
        config.setExposedHeaders(List.of("Authorization"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}