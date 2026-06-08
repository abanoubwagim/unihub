package com.unihub.company.api.controllers;

import com.unihub.company.api.dto.req.CreatePartnershipRequest;
import com.unihub.company.api.dto.req.ReviewPartnershipRequest;
import com.unihub.company.api.dto.res.PartnershipResponse;
import com.unihub.company.application.usecase.CompanyPartnershipUseCase;
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
@RequestMapping("/api/v1/companies/me/partnerships")
@RequiredArgsConstructor
@PreAuthorize("hasRole('COMPANY')")
public class CompanyPartnershipController {

    private final CompanyPartnershipUseCase partnershipUseCase;

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
    public ResponseEntity<PartnershipResponse> requestPartnership(
            Authentication authentication,
            @Valid @RequestBody CreatePartnershipRequest request) {
        UUID userId = UUID.fromString(authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(partnershipUseCase.requestPartnership(userId, request));
    }

    @PutMapping("/{partnershipId}/review")
    public ResponseEntity<PartnershipResponse> reviewPartnership(
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
        partnershipUseCase.terminate(userId, partnershipId);
        return ResponseEntity.noContent().build();
    }
}
