package com.unihub.shared.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Slf4j
@Component
public class LoginRateLimitFilter extends OncePerRequestFilter {

    private final StringRedisTemplate redis;

    @Value("${app.trusted-proxy-count:1}")
    private int trustedProxyCount;


    @Value("${app.rate-limit.login.max-attempts:10}")
    private int maxAttempts;

    @Value("${app.rate-limit.login.window-seconds:60}")
    private int windowSeconds;

    private static final String KEY_PREFIX = "login:attempts:";
    private static final String LOGIN_URI = "/api/auth/login";
    private static final String POST_METHOD = "POST";

    private static final DefaultRedisScript<Long> INCR_SCRIPT;
    static {
        INCR_SCRIPT = new DefaultRedisScript<>();
        INCR_SCRIPT.setScriptText(
                "local count = redis.call('INCR', KEYS[1]) " +
                        "if count == 1 then redis.call('EXPIRE', KEYS[1], ARGV[1]) end " +
                        "return count");
        INCR_SCRIPT.setResultType(Long.class);
    }

    public LoginRateLimitFilter(StringRedisTemplate redis) {
        this.redis = redis;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain)
            throws ServletException, IOException {

        if (!LOGIN_URI.equals(request.getRequestURI())
                || !POST_METHOD.equals(request.getMethod())) {
            chain.doFilter(request, response);
            return;
        }

        String clientIp = resolveClientIp(request);
        String key = KEY_PREFIX + clientIp;

        Long count = redis.execute(
                INCR_SCRIPT,
                List.of(key),
                String.valueOf(windowSeconds));

        if (count != null && count > maxAttempts) {
            log.warn("Login rate limit exceeded — ip={}, count={}, limit={}",
                    clientIp, count, maxAttempts);
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.getWriter().write("Too many login attempts. Please try again later.");
            return;
        }

        chain.doFilter(request, response);
    }

    private String resolveClientIp(HttpServletRequest request) {
        if (trustedProxyCount <= 0) {
            return request.getRemoteAddr();
        }
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded == null || forwarded.isBlank()) {
            return request.getRemoteAddr();
        }
        String[] parts = forwarded.split(",");
        int clientIndex = parts.length - trustedProxyCount;
        if (clientIndex < 0) {
            log.warn("X-Forwarded-For has fewer entries ({}) than trustedProxyCount ({}) — "
                    + "falling back to remoteAddr", parts.length, trustedProxyCount);
            return request.getRemoteAddr();
        }
        return parts[clientIndex].trim();
    }
}