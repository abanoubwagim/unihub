package com.unihub.student.domain.event;

import java.time.LocalDateTime;
import java.util.UUID;

public record GraduationCertificateSubmittedEvent(
        UUID certId,
        UUID studentProfileId,
        UUID universityId,
        String fileUrl,
        int attemptNumber,
        LocalDateTime submittedAt
) {
}
