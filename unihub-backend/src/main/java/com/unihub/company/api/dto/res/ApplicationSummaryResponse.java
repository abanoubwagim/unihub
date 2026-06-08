package com.unihub.company.api.dto.res;

import com.unihub.company.domain.enums.ApplicationStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record ApplicationSummaryResponse(
        UUID id,
        UUID jobPostingId,
        UUID studentProfileId,
        String cvUrl,
        ApplicationStatus status,
        String rejectionReason,
        LocalDateTime submittedAt,
        LocalDateTime reviewedAt
) {
}
