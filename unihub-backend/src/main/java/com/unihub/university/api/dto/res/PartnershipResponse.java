package com.unihub.university.api.dto.res;

import com.unihub.shared.domain.enums.PartnershipRequester;
import com.unihub.shared.domain.enums.PartnershipStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record PartnershipResponse(
        UUID id,
        UUID companyId,
        PartnershipStatus status,
        PartnershipRequester requestedBy,
        int hiredStudentCount,  // populated from CompanyPublicApi when company module is ready
        LocalDateTime createdAt
) {
}