package com.unihub.student.api.dto;

import java.time.LocalDate;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CertificationRequest(

    @NotBlank 
    String title,
    
    @NotBlank 
    String issuingOrganization,
    
    @NotNull 
    LocalDate dateIssued
) {}