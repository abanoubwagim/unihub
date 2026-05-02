package com.unihub.identity.application.impl;

import com.unihub.identity.application.usecase.LogoutUseCase;
import com.unihub.shared.security.JwtService;
import com.unihub.shared.security.TokenBlacklistService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LogoutUseCaseImpl implements LogoutUseCase {

    private final JwtService jwtService;
    private final TokenBlacklistService tokenBlacklistService;

    @Override
    public void logout(String token) {
        
        long ttl = jwtService.getExpirationSeconds(token);
        if (ttl > 0) {
            tokenBlacklistService.blacklist(token, ttl);
        }
    }
}