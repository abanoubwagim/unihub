package com.unihub.student.api.dto.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record CertificationRequest(

        @NotBlank(message = "Title is required")
        String title,

        @NotBlank(message = "Issuing organization is required")
        String issuingOrganization,

        @NotNull(message = "Date issued is required")
        LocalDate dateIssued
) {
}