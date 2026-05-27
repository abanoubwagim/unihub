package com.unihub.student.application;

import java.time.LocalDateTime;
import java.util.UUID;

public record PendingCertInfo(
        UUID certId,
        UUID studentProfileId,
        String fileUrl,
        int attemptNumber,
        LocalDateTime submittedAt
) {
}