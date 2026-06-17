package com.unihub.shared.api.dto.external;

import java.time.LocalDateTime;
import java.util.UUID;

public record PendingCertInfo(
        UUID certId,
        UUID studentProfileId,
        String studentName,
        String studentPhotoUrl,
        String fileUrl,
        int attemptNumber,
        LocalDateTime submittedAt
) {
}