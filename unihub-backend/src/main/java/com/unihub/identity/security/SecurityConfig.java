package com.unihub.identity.security;

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

    private final JwtAuthenticationFilter jwtFilter;
    private final UniHubOAuth2UserService oAuth2UserService;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final OAuth2FailureHandler oAuth2FailureHandler;
    private final LoginRateLimitFilter loginRateLimitFilter;
    private final ClientRegistrationRepository clientRegistrationRepository;

    @Value("${app.frontend-url:http://localhost:4200}")
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

    private static final String OAUTH2_BASE_URI = "/oauth2/authorize";

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())

                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/me").authenticated()
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/oauth2/**", "/login/oauth2/**").permitAll()
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**")
                        .permitAll()

                        // Student
                        .requestMatchers("/api/students/me/**").hasRole("STUDENT")
                        .requestMatchers(HttpMethod.GET, "/api/students/**").authenticated()
                        .requestMatchers("/api/students/**").hasRole("STUDENT")

                        // University
                        .requestMatchers("/api/university/profile/me/**").hasRole("UNIVERSITY")
                        .requestMatchers("/api/university/metadata/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/university/profile/**").authenticated()
                        .requestMatchers("/api/university/**").hasRole("UNIVERSITY")

                        .anyRequest().authenticated())
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