package com.unihub.student.api.controllers;

import com.unihub.company.api.dto.external.JobPostingPublicInfo;
import com.unihub.shared.api.dto.PageResponse;
import com.unihub.student.application.usecase.StudentJobUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/students/me/jobs")
@RequiredArgsConstructor
@PreAuthorize("hasRole('STUDENT')")
public class StudentJobController {

    private final StudentJobUseCase jobUseCase;

    @GetMapping
    public ResponseEntity<PageResponse<JobPostingPublicInfo>> getAvailableJobs(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        UUID userId = UUID.fromString(authentication.getName());
        Pageable pageable = PageRequest.of(
                page, Math.min(size, 50), Sort.by("publishedAt").descending());
        return ResponseEntity.ok(jobUseCase.getAvailableJobs(userId, pageable));
    }

    @GetMapping("/{jobPostingId}")
    public ResponseEntity<JobPostingPublicInfo> getJobDetail(
            Authentication authentication,
            @PathVariable UUID jobPostingId) {

        UUID userId = UUID.fromString(authentication.getName());
        return ResponseEntity.ok(jobUseCase.getJobDetail(userId, jobPostingId));
    }

    @PostMapping(value = "/{jobPostingId}/apply",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> apply(
            Authentication authentication,
            @PathVariable UUID jobPostingId,
            @RequestParam("cv") MultipartFile cvFile) {

        UUID userId = UUID.fromString(authentication.getName());
        jobUseCase.applyToJob(userId, jobPostingId, cvFile);
        return ResponseEntity.ok().build();
    }
}