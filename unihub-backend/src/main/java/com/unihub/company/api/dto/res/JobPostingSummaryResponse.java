package com.unihub.company.api.dto.res;

import com.unihub.company.domain.enums.JobPostingStatus;
import com.unihub.company.domain.enums.WorkLocationType;
import com.unihub.shared.domain.enums.JobType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record JobPostingSummaryResponse(
        UUID id,
        String title,
        JobType jobType,
        WorkLocationType workLocationType,
        JobPostingStatus status,
        int applicantCount,
        LocalDate deadline,
        LocalDateTime publishedAt
) {
}
