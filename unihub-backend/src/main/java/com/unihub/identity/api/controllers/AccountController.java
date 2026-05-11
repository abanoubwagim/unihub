package com.unihub.identity.api.controllers;

import java.util.UUID;


import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.unihub.identity.api.dto.DeleteAccountRequest;
import com.unihub.identity.application.usecase.DeleteAccountUseCase;
import com.unihub.identity.application.usecase.LogoutUseCase;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Validated
@RestController
@RequestMapping("/api/account")
@RequiredArgsConstructor
public class AccountController {

    private final DeleteAccountUseCase deleteAccountUseCase;
    private final LogoutUseCase logoutUseCase;

    @DeleteMapping("/me")
    public ResponseEntity<String> deleteAccount(Authentication authentication,
            @Valid @RequestBody DeleteAccountRequest request,
            HttpServletRequest httpRequest) {

        UUID userId = UUID.fromString(authentication.getName());

        // Step 1: delete the account (transactional — commits before we proceed)
        deleteAccountUseCase.deleteAccount(userId, request.password());

        // Step 2: best-effort blacklist of the current token
        String token = extractBearerToken(httpRequest);
        if (token != null) {
            logoutUseCase.logout(token); // catches Redis failures internally
        } else {
            log.warn("DeleteAccount succeeded but no Bearer token found to blacklist — userId={}", userId);
        }

        return ResponseEntity.ok("Account deleted successfully");
    }

    private String extractBearerToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }
}