package com.unihub.student.api.dto;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;

public record ProjectRequest(

    @NotBlank 
    String title,
    
    String description,
    LocalDate startDate,
    LocalDate endDate,
    String projectLink,
    Set<UUID> skillIds
) {}