package com.unihub.identity.api.controllers;

import java.util.UUID;


import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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
        deleteAccountUseCase.deleteAccount(userId, request.password());

        String authHeader = httpRequest.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            logoutUseCase.logout(authHeader.substring(7));
        }
        return ResponseEntity.ok("Account deleted successfully");

    }

}
