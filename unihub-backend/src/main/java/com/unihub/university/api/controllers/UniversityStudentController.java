package com.unihub.university.api.controllers;

import com.unihub.shared.api.dto.PageResponse;
import com.unihub.university.api.dto.res.UniversityStudentSummaryResponse;
import com.unihub.university.application.usecase.UniversityStudentUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/universities/me/students")
@RequiredArgsConstructor
@PreAuthorize("hasRole('UNIVERSITY')")
public class UniversityStudentController {

    private final UniversityStudentUseCase studentUseCase;

    @GetMapping
    public ResponseEntity<PageResponse<UniversityStudentSummaryResponse>> getMyStudents(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {

        UUID userId = UUID.fromString(authentication.getName());

        if (!Set.of("name", "level").contains(sortBy)) sortBy = "name";
        int safeSize = Math.min(size, 50);

        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, safeSize, sort);
        return ResponseEntity.ok(studentUseCase.getMyStudents(userId, pageable));
    }

    @GetMapping("/{studentProfileId}")
    public ResponseEntity<UniversityStudentSummaryResponse> getStudent(
            Authentication authentication,
            @PathVariable UUID studentProfileId) {
        UUID userId = UUID.fromString(authentication.getName());
        return ResponseEntity.ok(studentUseCase.getStudent(userId, studentProfileId));
    }
}