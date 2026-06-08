package com.unihub.company.domain.event;

import java.util.UUID;

public record JobApplicationSubmittedEvent(
        UUID jobPostingId,
        UUID companyProfileId,
        UUID studentProfileId
) {
}