package com.unihub.company.api.dto.external;

import com.unihub.company.domain.enums.WorkLocationType;
import com.unihub.shared.domain.enums.JobType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record JobPostingPublicInfo(
        UUID id,
        UUID companyId,
        String title,
        JobType jobType,
        WorkLocationType workLocationType,
        Integer salaryFrom,
        Integer salaryTo,
        String description,
        LocalDate deadline,
        int applicantCount,
        LocalDateTime publishedAt
) {
}