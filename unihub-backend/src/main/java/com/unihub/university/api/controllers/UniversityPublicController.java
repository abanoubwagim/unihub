package com.unihub.university.api.controllers;

import com.unihub.shared.api.dto.PageResponse;
import com.unihub.university.api.dto.res.UniversityPublicResponse;
import com.unihub.university.application.usecase.UniversityPublicUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/universities")
@RequiredArgsConstructor
public class UniversityPublicController {

    private final UniversityPublicUseCase publicUseCase;

    @GetMapping
    public ResponseEntity<PageResponse<UniversityPublicResponse>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        var pageable = PageRequest.of(
                page,
                Math.min(size, 50),
                Sort.by("name").ascending());

        return ResponseEntity.ok(publicUseCase.getAll(pageable));
    }
}