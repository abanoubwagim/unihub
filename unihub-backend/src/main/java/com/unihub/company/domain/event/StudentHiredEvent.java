package com.unihub.company.domain.event;

import java.util.UUID;

public record StudentHiredEvent(
        UUID jobPostingId,
        UUID companyProfileId,
        UUID studentProfileId
) {
}