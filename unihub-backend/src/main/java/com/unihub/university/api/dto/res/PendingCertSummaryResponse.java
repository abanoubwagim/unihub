package com.unihub.university.api.dto.res;

import java.time.LocalDateTime;
import java.util.UUID;

public record PendingCertSummaryResponse(
        UUID certId,
        UUID studentProfileId,
        String studentName,
        String studentPhotoUrl,
        String fileUrl,
        int attemptNumber,
        LocalDateTime submittedAt
) {
}