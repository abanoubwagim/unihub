package com.unihub.university.api.controllers;

import com.unihub.shared.api.dto.PageResponse;
import com.unihub.university.api.dto.req.ReviewCertificateRequest;
import com.unihub.university.api.dto.res.PendingCertSummaryResponse;
import com.unihub.university.application.usecase.UniversityGraduationUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/universities/me/graduation-certificates")
@RequiredArgsConstructor
@PreAuthorize("hasRole('UNIVERSITY')")
public class UniversityGraduationController {

    private final UniversityGraduationUseCase graduationUseCase;

    @GetMapping("/pending")
    public ResponseEntity<PageResponse<PendingCertSummaryResponse>> getPending(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        UUID userId = UUID.fromString(authentication.getName());
        int safeSize = Math.min(size, 50);
        Pageable pageable = PageRequest.of(page, safeSize, Sort.by("submittedAt").ascending());
        return ResponseEntity.ok(graduationUseCase.getPendingCertificates(userId, pageable));
    }

    @PutMapping("/{certId}/review")
    public ResponseEntity<Void> review(
            Authentication authentication,
            @PathVariable UUID certId,
            @Valid @RequestBody ReviewCertificateRequest request) {

        UUID userId = UUID.fromString(authentication.getName());
        graduationUseCase.reviewCertificate(userId, certId, request);
        return ResponseEntity.ok().build();
    }
}