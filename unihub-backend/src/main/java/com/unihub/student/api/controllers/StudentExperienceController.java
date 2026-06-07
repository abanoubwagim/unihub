package com.unihub.student.api.controllers;

import com.unihub.shared.api.dto.PageResponse;
import com.unihub.student.api.dto.req.ExperienceRequest;
import com.unihub.student.api.dto.res.ExperienceResponse;
import com.unihub.student.application.usecase.StudentExperienceUseCase;
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

import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/students/me/experiences")
@RequiredArgsConstructor
@PreAuthorize("hasRole('STUDENT')")
public class StudentExperienceController {

    private final StudentExperienceUseCase experienceUseCase;

    @GetMapping
    public ResponseEntity<PageResponse<ExperienceResponse>> getAll(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "startDate") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {

        UUID userId = UUID.fromString(authentication.getName());

        // Protect against invalid sort fields
        if (!Set.of("id", "startDate", "endDate", "company", "jobTitle").contains(sortBy)) {
            sortBy = "startDate";
        }
        int safeSize = Math.min(size, 50); // Max 50 per page

        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, safeSize, sort);
        return ResponseEntity.ok(experienceUseCase.getAll(userId, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExperienceResponse> getOne(
            Authentication authentication,
            @PathVariable UUID id) {
        UUID userId = UUID.fromString(authentication.getName());
        return ResponseEntity.ok(experienceUseCase.getOne(userId, id));
    }

    @PostMapping
    public ResponseEntity<ExperienceResponse> add(
            Authentication authentication,
            @Valid @RequestBody ExperienceRequest request) {
        UUID userId = UUID.fromString(authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(experienceUseCase.add(userId, request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ExperienceResponse> update(
            Authentication authentication,
            @PathVariable UUID id,
            @Valid @RequestBody ExperienceRequest request) {
        UUID userId = UUID.fromString(authentication.getName());
        return ResponseEntity.ok(experienceUseCase.update(userId, id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            Authentication authentication,
            @PathVariable UUID id) {
        UUID userId = UUID.fromString(authentication.getName());
        experienceUseCase.delete(userId, id);
        return ResponseEntity.noContent().build();
    }
}