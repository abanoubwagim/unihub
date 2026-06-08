package com.unihub.company.api.controllers;

import com.unihub.company.api.dto.req.CreateJobPostingRequest;
import com.unihub.company.api.dto.req.UpdateJobPostingRequest;
import com.unihub.company.api.dto.res.JobPostingResponse;
import com.unihub.company.api.dto.res.JobPostingSummaryResponse;
import com.unihub.company.application.usecase.CompanyJobPostingUseCase;
import com.unihub.company.domain.enums.JobPostingStatus;
import com.unihub.shared.api.dto.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/companies/me/job-postings")
@RequiredArgsConstructor
@PreAuthorize("hasRole('COMPANY')")
public class CompanyJobPostingController {

    private final CompanyJobPostingUseCase jobPostingUseCase;

    @GetMapping
    public ResponseEntity<PageResponse<JobPostingSummaryResponse>> getAll(
            Authentication authentication,
            @RequestParam(required = false) JobPostingStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        UUID userId = UUID.fromString(authentication.getName());
        Pageable pageable = PageRequest.of(page, Math.min(size, 50),
                Sort.by("createdAt").descending());
        return ResponseEntity.ok(jobPostingUseCase.getAll(userId, status, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<JobPostingResponse> getById(
            Authentication authentication,
            @PathVariable UUID id) {
        UUID userId = UUID.fromString(authentication.getName());
        return ResponseEntity.ok(jobPostingUseCase.getById(userId, id));
    }

    @PostMapping
    public ResponseEntity<JobPostingResponse> create(
            Authentication authentication,
            @RequestBody @Valid CreateJobPostingRequest request) {
        UUID userId = UUID.fromString(authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(jobPostingUseCase.createDraft(userId, request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<JobPostingResponse> updateDraft(
            Authentication authentication,
            @PathVariable UUID id,
            @RequestBody UpdateJobPostingRequest request) {
        UUID userId = UUID.fromString(authentication.getName());
        return ResponseEntity.ok(jobPostingUseCase.updateDraft(userId, id, request));
    }

    @PostMapping("/{id}/publish")
    public ResponseEntity<JobPostingResponse> publish(
            Authentication authentication,
            @PathVariable UUID id) {
        UUID userId = UUID.fromString(authentication.getName());
        return ResponseEntity.ok(jobPostingUseCase.publish(userId, id));
    }

    @PostMapping("/{id}/close")
    public ResponseEntity<JobPostingResponse> close(
            Authentication authentication,
            @PathVariable UUID id) {
        UUID userId = UUID.fromString(authentication.getName());
        return ResponseEntity.ok(jobPostingUseCase.close(userId, id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            Authentication authentication,
            @PathVariable UUID id) {
        UUID userId = UUID.fromString(authentication.getName());
        jobPostingUseCase.delete(userId, id);
        return ResponseEntity.noContent().build();
    }
}
