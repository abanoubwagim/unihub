package com.unihub.company.application.usecase;

import com.unihub.company.api.dto.req.CreatePartnershipRequest;
import com.unihub.company.api.dto.req.ReviewPartnershipRequest;
import com.unihub.company.api.dto.res.PartnershipResponse;
import com.unihub.shared.api.dto.PageResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface CompanyPartnershipUseCase {

    PageResponse<PartnershipResponse> getAll(UUID userId, Pageable pageable);

    PartnershipResponse requestPartnership(UUID userId, CreatePartnershipRequest request);

    PartnershipResponse reviewPartnership(UUID userId, UUID partnershipId, ReviewPartnershipRequest request);

    void terminate(UUID userId, UUID partnershipId);
}