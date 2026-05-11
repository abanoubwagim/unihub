package com.unihub.identity.application.impl;

import com.unihub.identity.application.usecase.LogoutUseCase;
import com.unihub.shared.security.JwtService;
import com.unihub.shared.security.TokenBlacklistService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class LogoutUseCaseImpl implements LogoutUseCase {

    private final JwtService jwtService;
    private final TokenBlacklistService tokenBlacklistService;

    @Override
    public void logout(String token) {
        try {
            long ttl = jwtService.getExpirationSeconds(token);
            if (ttl > 0) {
                tokenBlacklistService.blacklist(token, ttl);
                log.debug("Token blacklisted successfully — ttl={}s", ttl);
            } else {
                log.debug("Token already expired — skipping blacklist");
            }
        } catch (Exception e) {
            log.error("Logout blacklist failed — token may be invalid or Redis unavailable. error={}",
                    e.getMessage());
        }
    }
}