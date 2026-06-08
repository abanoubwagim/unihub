package com.unihub.company.api.dto.res;

import com.unihub.shared.domain.enums.PartnershipRequester;
import com.unihub.shared.domain.enums.PartnershipStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record PartnershipResponse(
        UUID partnershipId,
        UUID universityProfileId,
        PartnershipStatus status,
        PartnershipRequester requestedBy,
        LocalDateTime createdAt
) {
}
