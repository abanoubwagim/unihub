package com.unihub.student.api.controllers;

import com.unihub.student.api.dto.req.LinkRequest;
import com.unihub.student.api.dto.res.LinkResponse;
import com.unihub.student.application.usecase.StudentLinkUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/students/me/links")
@RequiredArgsConstructor
@PreAuthorize("hasRole('STUDENT')")
public class StudentLinkController {

    private final StudentLinkUseCase linkUseCase;

    @GetMapping
    public ResponseEntity<List<LinkResponse>> getAll(Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        return ResponseEntity.ok(linkUseCase.getAll(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<LinkResponse> getOne(
            Authentication authentication,
            @PathVariable UUID id) {
        UUID userId = UUID.fromString(authentication.getName());
        return ResponseEntity.ok(linkUseCase.getOne(userId, id));
    }

    @PostMapping
    public ResponseEntity<LinkResponse> add(
            Authentication authentication,
            @Valid @RequestBody LinkRequest request) {
        UUID userId = UUID.fromString(authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(linkUseCase.add(userId, request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<LinkResponse> update(
            Authentication authentication,
            @PathVariable UUID id,
            @Valid @RequestBody LinkRequest request) {
        UUID userId = UUID.fromString(authentication.getName());
        return ResponseEntity.ok(linkUseCase.update(userId, id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            Authentication authentication,
            @PathVariable UUID id) {
        UUID userId = UUID.fromString(authentication.getName());
        linkUseCase.delete(userId, id);
        return ResponseEntity.noContent().build();
    }
}