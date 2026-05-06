package com.unihub.identity.application.impl;

import com.unihub.shared.security.JwtService;
import com.unihub.shared.security.TokenBlacklistService;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("LogoutUseCase Tests")
class LogoutUseCaseTest {

    @Mock
    private JwtService jwtService;
    @Mock
    private TokenBlacklistService tokenBlacklistService;

    @InjectMocks
    private LogoutUseCaseImpl logoutUseCase;

    @Test
    @DisplayName("should blacklist token with correct TTL when token is still valid")
    void shouldBlacklistValidToken() {

        String token = "valid.jwt.token";
        when(jwtService.getExpirationSeconds(token)).thenReturn(3600L);

        logoutUseCase.logout(token);

        verify(tokenBlacklistService).blacklist(token, 3600L);
    }

    @Test
    @DisplayName("should NOT blacklist token when it is already expired (ttl = 0)")
    void shouldNotBlacklistExpiredToken() {
        
        String token = "expired.jwt.token";
        when(jwtService.getExpirationSeconds(token)).thenReturn(0L);

        logoutUseCase.logout(token);

        verify(tokenBlacklistService, never()).blacklist(any(), anyLong());
    }

    @Test
    @DisplayName("should NOT blacklist token when ttl is negative")
    void shouldNotBlacklistNegativeTtlToken() {
        String token = "some.token";
        when(jwtService.getExpirationSeconds(token)).thenReturn(-1L);

        logoutUseCase.logout(token);

        verify(tokenBlacklistService, never()).blacklist(any(), anyLong());
    }
}