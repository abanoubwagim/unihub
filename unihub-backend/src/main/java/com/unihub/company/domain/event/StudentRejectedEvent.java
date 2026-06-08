package com.unihub.company.domain.event;

import java.util.UUID;

public record StudentRejectedEvent(
        UUID jobPostingId,
        UUID companyProfileId,
        UUID studentProfileId,
        String rejectionReason
) {
}