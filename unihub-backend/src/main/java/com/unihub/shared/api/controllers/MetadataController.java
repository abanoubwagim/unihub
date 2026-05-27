package com.unihub.shared.api.controllers;

import com.unihub.shared.api.dto.CountryResponse;
import com.unihub.shared.application.CountryUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/metadata")
public class MetadataController {

    private final CountryUseCase countryUseCase;

    @GetMapping("/countries")
    public ResponseEntity<List<CountryResponse>> getCountries() {
        return ResponseEntity.ok(countryUseCase.getAll());
    }
}
