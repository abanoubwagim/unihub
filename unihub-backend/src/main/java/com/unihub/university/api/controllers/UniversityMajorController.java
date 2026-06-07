package com.unihub.university.api.controllers;

import com.unihub.university.api.dto.res.MajorResponse;
import com.unihub.university.application.usecase.UniversityMajorUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/universities/me/majors")
@RequiredArgsConstructor
@PreAuthorize("hasRole('UNIVERSITY')")
public class UniversityMajorController {

    private final UniversityMajorUseCase majorUseCase;

    @GetMapping("/available")
    public ResponseEntity<List<MajorResponse>> getAvailable() {
        return ResponseEntity.ok(majorUseCase.getAllAvailable());
    }

    @GetMapping
    public ResponseEntity<List<MajorResponse>> getSelected(Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        return ResponseEntity.ok(majorUseCase.getSelected(userId));
    }

    @PostMapping("/{majorId}")
    public ResponseEntity<Void> select(
            Authentication authentication,
            @PathVariable UUID majorId) {
        UUID userId = UUID.fromString(authentication.getName());
        majorUseCase.select(userId, majorId);
        return ResponseEntity.noContent().build();
    }
    

}