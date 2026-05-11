package com.unihub.identity.application.impl;

import com.unihub.identity.api.dto.OAuth2TokenResponse;
import com.unihub.identity.application.usecase.ExchangeOAuth2CodeUseCase;
import com.unihub.shared.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExchangeOAuth2CodeUseCaseImpl implements ExchangeOAuth2CodeUseCase {

    private static final String OAUTH2_CODE_PREFIX = "oauth2:code:";

    private final StringRedisTemplate redisTemplate;

    @Override
    public OAuth2TokenResponse exchange(String code) {
        if (code == null || code.isBlank()) {
            throw new BadRequestException("Invalid authorization code");
        }
        String redisKey = OAUTH2_CODE_PREFIX + code;

        // getAndDelete is atomic — one-time use guaranteed
        String jwt = redisTemplate.opsForValue().getAndDelete(redisKey);

        if (jwt == null) {
            log.warn("OAuth2 code exchange failed — code not found or already used");
            throw new BadRequestException("Authorization code is invalid or has expired");
        }

        log.debug("OAuth2 code exchanged successfully");
        return new OAuth2TokenResponse(jwt, "Bearer");
    }
}