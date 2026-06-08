package com.unihub.company.api.dto.req;

import com.unihub.company.domain.enums.WorkLocationType;
import com.unihub.shared.domain.enums.JobType;

import java.time.LocalDate;

public record UpdateJobPostingRequest(
        String title,
        JobType jobType,
        WorkLocationType workLocationType,
        Integer salaryFrom,
        Integer salaryTo,
        String description,
        LocalDate deadline
) {
}
