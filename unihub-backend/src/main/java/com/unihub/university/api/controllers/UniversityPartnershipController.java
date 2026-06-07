package com.unihub.university.api.controllers;

import com.unihub.shared.api.dto.PageResponse;
import com.unihub.university.api.dto.req.CreatePartnershipRequest;
import com.unihub.university.api.dto.req.ReviewPartnershipRequest;
import com.unihub.university.api.dto.res.PartnershipResponse;
import com.unihub.university.application.usecase.UniversityPartnershipUseCase;
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
@RequestMapping("/api/v1/universities/me/partnerships")
@RequiredArgsConstructor
@PreAuthorize("hasRole('UNIVERSITY')")
public class UniversityPartnershipController {

    private final UniversityPartnershipUseCase partnershipUseCase;

    @GetMapping
    public ResponseEntity<PageResponse<PartnershipResponse>> getAll(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        UUID userId = UUID.fromString(authentication.getName());
        Pageable pageable = PageRequest.of(page, Math.min(size, 50), Sort.by("createdAt").descending());
        return ResponseEntity.ok(partnershipUseCase.getAll(userId, pageable));
    }

    @PostMapping
    public ResponseEntity<PartnershipResponse> request(
            Authentication authentication,
            @Valid @RequestBody CreatePartnershipRequest request) {
        UUID userId = UUID.fromString(authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(partnershipUseCase.requestPartnership(userId, request));
    }

    @PutMapping("/{partnershipId}/review")
    public ResponseEntity<PartnershipResponse> review(
            Authentication authentication,
            @PathVariable UUID partnershipId,
            @Valid @RequestBody ReviewPartnershipRequest request) {
        UUID userId = UUID.fromString(authentication.getName());
        return ResponseEntity.ok(partnershipUseCase.reviewPartnership(userId, partnershipId, request));
    }

    @DeleteMapping("/{partnershipId}")
    public ResponseEntity<Void> terminate(
            Authentication authentication,
            @PathVariable UUID partnershipId) {
        UUID userId = UUID.fromString(authentication.getName());
        partnershipUseCase.terminatePartnership(userId, partnershipId);
        return ResponseEntity.noContent().build();
    }
}