package com.unihub.student.api.dto.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

public record ProjectRequest(

        @NotBlank(message = "Title is required")
        String title,

        String description,

        @NotNull(message = "Start date is required")
        LocalDate startDate,

        LocalDate endDate,

        @NotNull(message = "Current status is required")
        Boolean current,

        String projectLink,

        Set<UUID> skillIds
) {
}