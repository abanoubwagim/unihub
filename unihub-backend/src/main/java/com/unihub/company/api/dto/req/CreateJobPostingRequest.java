package com.unihub.company.api.dto.req;

import com.unihub.company.domain.enums.WorkLocationType;
import com.unihub.shared.domain.enums.JobType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record CreateJobPostingRequest(

        @NotBlank(message = "Title is required")
        String title,

        @NotNull(message = "Job type is required")
        JobType jobType,

        @NotNull(message = "Work Location type is required")
        WorkLocationType workLocationType,

        @NotNull(message = "salaryFrom is required")
        Integer salaryFrom,

        @NotNull(message = "salaryFrom is required")
        Integer salaryTo,

        @NotBlank(message = "description is required")
        String description,
        LocalDate deadline,
        boolean publishNow
) {
}
