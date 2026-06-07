package com.unihub.student.domain.event;

import java.util.UUID;

public record CertificateReviewedEvent(
        UUID certId,
        UUID studentProfileId,
        UUID universityId,
        boolean approved,
        String rejectionReason
) {
}
