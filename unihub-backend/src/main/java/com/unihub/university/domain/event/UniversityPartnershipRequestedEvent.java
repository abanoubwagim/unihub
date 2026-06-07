package com.unihub.university.domain.event;

import java.util.UUID;

public record UniversityPartnershipRequestedEvent(
        UUID partnershipId,
        UUID universityId,
        UUID companyId
) {
}