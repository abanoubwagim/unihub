package com.unihub.identity.api.controllers;


import com.unihub.identity.application.usecase.DeleteAccountUseCase;
import com.unihub.identity.application.usecase.LogoutUseCase;
import com.unihub.identity.application.usecase.RefreshTokenUseCase;
import com.unihub.shared.api.dto.DeleteAccountRequest;
import com.unihub.shared.util.CookieUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Slf4j
@Validated
@RestController
@RequestMapping("/api/v1/account")
@RequiredArgsConstructor
public class AccountController {

    private final DeleteAccountUseCase deleteAccountUseCase;
    private final LogoutUseCase logoutUseCase;
    private final RefreshTokenUseCase refreshTokenUseCase;


    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteAccount(
            Authentication authentication,
            @Valid @RequestBody DeleteAccountRequest request,
            HttpServletRequest httpRequest) {

        UUID userId = UUID.fromString(authentication.getName());

        // Step 1: delete the account (transactional)
        deleteAccountUseCase.deleteAccount(userId, request.password());

        // Step 2: revoke all refresh tokens (safety net — may already be cascaded)
        try {
            refreshTokenUseCase.revokeAllForUser(userId);
        } catch (Exception e) {
            log.warn("DeleteAccount: best-effort refresh token revocation failed — userId={}", userId);
        }

        // Step 3: blacklist the current access token
        String accessToken = refreshTokenUseCase.extractBearerToken(httpRequest);
        if (accessToken != null) {
            logoutUseCase.logout(accessToken);
        } else {
            log.warn("DeleteAccount succeeded but no Bearer token found to blacklist — userId={}", userId);
        }

        // Step 4: clear the refresh token cookie on the client
        ResponseCookie expiredCookie = CookieUtil.buildExpiredRefreshCookie();

        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, expiredCookie.toString())
                .build();
    }


}