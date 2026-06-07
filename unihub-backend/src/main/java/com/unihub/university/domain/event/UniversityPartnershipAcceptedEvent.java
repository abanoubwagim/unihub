package com.unihub.university.domain.event;

import java.util.UUID;

public record UniversityPartnershipAcceptedEvent(
        UUID partnershipId,
        UUID universityId,
        UUID companyId
) {
}