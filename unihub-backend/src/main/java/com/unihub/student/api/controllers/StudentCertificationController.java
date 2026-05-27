package com.unihub.student.api.controllers;

import com.unihub.shared.dto.PageResponse;
import com.unihub.student.api.dto.req.CertificationRequest;
import com.unihub.student.api.dto.res.CertificationResponse;
import com.unihub.student.application.usecase.StudentCertificationUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/students/me/certifications")
@RequiredArgsConstructor
@PreAuthorize("hasRole('STUDENT')")
public class StudentCertificationController {

    private final StudentCertificationUseCase certificationUseCase;

    @GetMapping
    public ResponseEntity<PageResponse<CertificationResponse>> getAll(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "dateIssued") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {

        UUID userId = UUID.fromString(authentication.getName());

        // Protect against invalid sort fields
        if (!Set.of("id", "title", "dateIssued", "issuingOrganization").contains(sortBy)) {
            sortBy = "dateIssued";
        }
        int safeSize = Math.min(size, 50);

        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, safeSize, sort);
        return ResponseEntity.ok(certificationUseCase.getAll(userId, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CertificationResponse> getOne(
            Authentication authentication,
            @PathVariable UUID id) {
        UUID userId = UUID.fromString(authentication.getName());
        return ResponseEntity.ok(certificationUseCase.getOne(userId, id));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CertificationResponse> add(
            Authentication authentication,
            @RequestPart("data") @Valid CertificationRequest request,
            @RequestParam(value = "file", required = false) MultipartFile file) {
        UUID userId = UUID.fromString(authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(certificationUseCase.add(userId, request, file));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CertificationResponse> update(
            Authentication authentication,
            @PathVariable UUID id,
            @RequestPart("data") @Valid CertificationRequest request,
            @RequestParam(value = "file", required = false) MultipartFile file) {
        UUID userId = UUID.fromString(authentication.getName());
        return ResponseEntity.ok(certificationUseCase.update(userId, id, request, file));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            Authentication authentication,
            @PathVariable UUID id) {
        UUID userId = UUID.fromString(authentication.getName());
        certificationUseCase.delete(userId, id);
        return ResponseEntity.noContent().build();
    }
}