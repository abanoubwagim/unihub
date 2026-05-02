package com.unihub.student.api.dto;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

import com.unihub.shared.enums.JobType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ExperienceRequest(

    @NotBlank 
    String jobTitle,
    
    @NotBlank 
    String company,
    
    @NotNull 
    JobType jobType,
    
    @NotNull 
    LocalDate startDate,
    
    LocalDate endDate,
    boolean current,
    String location,
    String description,
    Set<UUID> skillIds
) {}