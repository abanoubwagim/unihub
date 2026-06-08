package com.unihub.company.api.controllers;

import com.unihub.company.api.dto.req.ReviewApplicationRequest;
import com.unihub.company.api.dto.res.ApplicationSummaryResponse;
import com.unihub.company.application.usecase.CompanyApplicationUseCase;
import com.unihub.shared.api.dto.PageResponse;
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
@RequestMapping("/api/v1/companies/me/job-postings/{jobPostingId}/applications")
@RequiredArgsConstructor
@PreAuthorize("hasRole('COMPANY')")
public class CompanyApplicationController {

    private final CompanyApplicationUseCase applicationUseCase;

    @GetMapping
    public ResponseEntity<PageResponse<ApplicationSummaryResponse>> getApplications(
            Authentication authentication,
            @PathVariable UUID jobPostingId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        UUID userId = UUID.fromString(authentication.getName());
        Pageable pageable = PageRequest.of(page, Math.min(size, 50), Sort.by("submittedAt").ascending());
        return ResponseEntity.ok(applicationUseCase.getApplications(userId, jobPostingId, pageable));
    }

    @GetMapping("/{applicationId}")
    public ResponseEntity<ApplicationSummaryResponse> getApplication(
            Authentication authentication,
            @PathVariable UUID jobPostingId,
            @PathVariable UUID applicationId) {

        UUID userId = UUID.fromString(authentication.getName());
        return ResponseEntity.ok(applicationUseCase.getApplication(userId, jobPostingId, applicationId));
    }

    @PutMapping("/{applicationId}/review")
    public ResponseEntity<ApplicationSummaryResponse> review(
            Authentication authentication,
            @PathVariable UUID jobPostingId,
            @PathVariable UUID applicationId,
            @Valid @RequestBody ReviewApplicationRequest request) {

        UUID userId = UUID.fromString(authentication.getName());
        return ResponseEntity.ok(applicationUseCase.review(userId, jobPostingId, applicationId, request));
    }
}
