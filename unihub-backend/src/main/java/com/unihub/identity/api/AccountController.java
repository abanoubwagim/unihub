package com.unihub.identity.api;

import java.util.UUID;


import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.unihub.identity.api.dto.DeleteAccountRequest;
import com.unihub.identity.application.DeleteAccountUseCase;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/account")
@RequiredArgsConstructor
public class AccountController {

    private final DeleteAccountUseCase deleteAccountUseCase;

    @DeleteMapping("/me")
    public ResponseEntity<String> deleteAccount(Authentication authentication,
            @Valid @RequestBody DeleteAccountRequest request) {

        UUID userId = UUID.fromString(authentication.getName());
        deleteAccountUseCase.deleteAccount(userId, request.password());
        return ResponseEntity.ok("Account deleted successfully");

    }

}
