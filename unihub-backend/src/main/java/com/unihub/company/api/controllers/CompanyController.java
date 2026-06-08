package com.unihub.company.api.controllers;

import com.unihub.company.api.dto.req.UpdateProfileRequest;
import com.unihub.company.api.dto.res.CompanyProfileResponse;
import com.unihub.company.application.usecase.CompanyProfileUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/companies")
@RequiredArgsConstructor
@PreAuthorize("hasRole('COMPANY')")
public class CompanyController {

    private final CompanyProfileUseCase profileUseCase;

    @GetMapping("/me")
    public ResponseEntity<CompanyProfileResponse> getMyProfile(Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        return ResponseEntity.ok(profileUseCase.getMyProfile(userId));
    }

    @PutMapping("/me")
    public ResponseEntity<CompanyProfileResponse> updateProfile(
            Authentication authentication,
            @Valid @RequestBody UpdateProfileRequest request) {
        UUID userId = UUID.fromString(authentication.getName());
        return ResponseEntity.ok(profileUseCase.updateProfile(userId, request));
    }

    @PostMapping(value = "/me/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadPhoto(
            Authentication authentication,
            @RequestParam("file") MultipartFile file) {
        UUID userId = UUID.fromString(authentication.getName());
        return ResponseEntity.ok(profileUseCase.uploadPhoto(userId, file));
    }
}
