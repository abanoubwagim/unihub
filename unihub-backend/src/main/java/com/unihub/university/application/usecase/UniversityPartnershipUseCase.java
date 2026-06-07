package com.unihub.university.application.usecase;

import com.unihub.shared.api.dto.PageResponse;
import com.unihub.university.api.dto.req.CreatePartnershipRequest;
import com.unihub.university.api.dto.req.ReviewPartnershipRequest;
import com.unihub.university.api.dto.res.PartnershipResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface UniversityPartnershipUseCase {

    PageResponse<PartnershipResponse> getAll(UUID userId, Pageable pageable);

    PartnershipResponse requestPartnership(UUID userId, CreatePartnershipRequest request);

    PartnershipResponse reviewPartnership(UUID userId, UUID partnershipId, ReviewPartnershipRequest request);

    void terminatePartnership(UUID userId, UUID partnershipId);
}