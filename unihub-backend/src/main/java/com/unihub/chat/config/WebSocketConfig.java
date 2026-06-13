package com.unihub.chat.config;

import com.unihub.shared.security.JwtSubject;
import com.unihub.shared.security.service.JwtService;
import com.unihub.shared.security.service.TokenBlacklistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.util.Collections;
import java.util.Optional;

@Slf4j
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtService jwtService;
    private final TokenBlacklistService tokenBlacklistService;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/queue", "/topic");
        registry.setApplicationDestinationPrefixes("/app");
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOrigins(frontendUrl)
                .withSockJS()
                .setSessionCookieNeeded(false);
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {

            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(
                        message, StompHeaderAccessor.class);

                if (accessor == null || !StompCommand.CONNECT.equals(accessor.getCommand())) {
                    return message;
                }

                String authHeader = accessor.getFirstNativeHeader("Authorization");
                if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                    log.warn("WebSocket CONNECT rejected — missing or malformed Authorization header");
                    throw new MessageDeliveryException(message,
                            "WebSocket CONNECT rejected: missing or malformed Authorization header");
                }

                String token = authHeader.substring(7);

                // Single parse — validates signature + expiry and extracts all claims at once
                Optional<JwtSubject> subjectOpt = jwtService.parseAndValidate(token);
                if (subjectOpt.isEmpty()) {
                    log.warn("WebSocket CONNECT rejected — invalid or expired token");
                    throw new MessageDeliveryException(message,
                            "WebSocket CONNECT rejected: invalid or expired token");
                }

                JwtSubject subject = subjectOpt.get();

                if (tokenBlacklistService.isBlacklisted(token)) {
                    log.warn("WebSocket CONNECT rejected — blacklisted token: userId={}", subject.id());
                    throw new MessageDeliveryException(message,
                            "WebSocket CONNECT rejected: blacklisted token");
                }

                // Guard against tokens issued before a password change / forced logout
                if (tokenBlacklistService.isTokenIssuedBeforeInvalidation(
                        subject.id().toString(), subject.issuedAtEpochSeconds())) {
                    log.warn("WebSocket CONNECT rejected — token predates invalidation: userId={}", subject.id());
                    throw new MessageDeliveryException(message,
                            "WebSocket CONNECT rejected: token invalidated");
                }

                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                        subject.id().toString(),
                        null,
                        Collections.singleton(new SimpleGrantedAuthority("ROLE_" + subject.role())));

                accessor.setUser(auth);
                log.debug("WebSocket CONNECT accepted — userId={}, role={}", subject.id(), subject.role());
                return message;
            }
        });
    }
}