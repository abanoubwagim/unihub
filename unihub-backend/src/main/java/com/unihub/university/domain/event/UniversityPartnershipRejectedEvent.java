package com.unihub.university.domain.event;

import java.util.UUID;

public record UniversityPartnershipRejectedEvent(
        UUID partnershipId,
        UUID universityId,
        UUID companyId
) {
}