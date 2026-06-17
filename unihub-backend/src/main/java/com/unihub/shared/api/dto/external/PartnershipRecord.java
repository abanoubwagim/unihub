package com.unihub.shared.api.dto.external;

import com.unihub.shared.domain.enums.PartnershipRequester;
import com.unihub.shared.domain.enums.PartnershipStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record PartnershipRecord(
        UUID partnershipId,
        UUID universityProfileId,
        UUID companyProfileId,
        PartnershipStatus status,
        PartnershipRequester requestedBy,
        LocalDateTime createdAt
) {
}