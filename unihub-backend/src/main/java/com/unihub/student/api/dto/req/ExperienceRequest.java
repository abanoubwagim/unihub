package com.unihub.student.api.dto.req;

import com.unihub.shared.domain.enums.JobType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

public record ExperienceRequest(

        @NotBlank(message = "Job title is required")
        String jobTitle,

        @NotBlank(message = "Company is required")
        String company,

        @NotNull(message = "Job type is required")
        JobType jobType,

        @NotNull(message = "Start date is required")
        LocalDate startDate,

        LocalDate endDate,

        @NotNull(message = "Current status is required")
        Boolean current,

        String location,

        String description,

        Set<UUID> skillIds
) {
}