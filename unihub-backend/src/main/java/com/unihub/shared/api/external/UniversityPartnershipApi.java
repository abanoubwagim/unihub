package com.unihub.shared.api.external;

import com.unihub.shared.api.dto.external.PartnershipRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Set;
import java.util.UUID;


public interface UniversityPartnershipApi {

    PartnershipRecord requestPartnership(UUID companyProfileId, UUID universityProfileId);

    PartnershipRecord reviewPartnership(UUID companyProfileId, UUID partnershipId, boolean accept);

    void terminatePartnership(UUID companyProfileId, UUID partnershipId);

    Page<PartnershipRecord> getAllForCompany(UUID companyProfileId, Pageable pageable);

    Set<UUID> getActivePartnerCompanyIds(UUID universityProfileId);

    boolean isMajorOfferedByUniversity(UUID universityId, UUID majorId);

}