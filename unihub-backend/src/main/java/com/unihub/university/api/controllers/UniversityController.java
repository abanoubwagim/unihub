package com.unihub.university.api.controllers;

import com.unihub.university.api.dto.req.UpdateProfileRequest;
import com.unihub.university.api.dto.res.UniversityProfileResponse;
import com.unihub.university.application.usecase.UniversityProfileUseCase;
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
@RequestMapping("/api/v1/universities")
@RequiredArgsConstructor
@PreAuthorize("hasRole('UNIVERSITY')")
public class UniversityController {

    private final UniversityProfileUseCase profileUseCase;

    @GetMapping("/me")
    public ResponseEntity<UniversityProfileResponse> getMyProfile(Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        return ResponseEntity.ok(profileUseCase.getMyProfile(userId));
    }

    @PutMapping("/me")
    public ResponseEntity<UniversityProfileResponse> updateProfile(
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