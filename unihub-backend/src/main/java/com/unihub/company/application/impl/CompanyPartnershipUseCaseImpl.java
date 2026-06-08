package com.unihub.company.application.impl;

import com.unihub.company.api.dto.req.CreatePartnershipRequest;
import com.unihub.company.api.dto.req.ReviewPartnershipRequest;
import com.unihub.company.api.dto.res.PartnershipResponse;
import com.unihub.company.application.usecase.CompanyPartnershipUseCase;
import com.unihub.company.domain.model.CompanyProfile;
import com.unihub.company.domain.repository.CompanyProfileRepository;
import com.unihub.shared.api.dto.PageResponse;
import com.unihub.shared.api.dto.external.PartnershipRecord;
import com.unihub.shared.api.external.UniversityPartnershipApi;
import com.unihub.shared.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CompanyPartnershipUseCaseImpl implements CompanyPartnershipUseCase {

    private final CompanyProfileRepository companyProfileRepository;
    private final UniversityPartnershipApi universityPartnershipApi;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PartnershipResponse> getAll(UUID userId, Pageable pageable) {
        CompanyProfile profile = getProfileByUserId(userId);
        return PageResponse.from(
                universityPartnershipApi
                        .getAllForCompany(profile.getId(), pageable)
                        .map(this::toResponse));
    }

    @Override
    public PartnershipResponse requestPartnership(UUID userId, CreatePartnershipRequest request) {
        log.debug("Company requesting partnership — userId={}, universityId={}", userId, request.universityId());

        CompanyProfile profile = getProfileByUserId(userId);
        PartnershipRecord partnershipRecord = universityPartnershipApi
                .requestPartnership(profile.getId(), request.universityId());

        log.info("Partnership requested — companyId={}, universityId={}, partnershipId={}",
                profile.getId(), request.universityId(), partnershipRecord.partnershipId());
        return toResponse(partnershipRecord);
    }

    @Override
    public PartnershipResponse reviewPartnership(UUID userId, UUID partnershipId, ReviewPartnershipRequest request) {
        log.debug("Company reviewing partnership — userId={}, partnershipId={}, accept={}",
                userId, partnershipId, request.accept());

        CompanyProfile profile = getProfileByUserId(userId);
        PartnershipRecord partnershipRecord = universityPartnershipApi
                .reviewPartnership(profile.getId(), partnershipId, request.accept());

        log.info("Partnership reviewed — partnershipId={}, accept={}", partnershipId, request.accept());
        return toResponse(partnershipRecord);
    }

    @Override
    public void terminate(UUID userId, UUID partnershipId) {
        log.debug("Company terminating partnership — userId={}, partnershipId={}", userId, partnershipId);

        CompanyProfile profile = getProfileByUserId(userId);
        universityPartnershipApi.terminatePartnership(profile.getId(), partnershipId);

        log.info("Partnership terminated — companyId={}, partnershipId={}", profile.getId(), partnershipId);
    }

    private CompanyProfile getProfileByUserId(UUID userId) {
        return companyProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("Company profile not found"));
    }

    private PartnershipResponse toResponse(PartnershipRecord r) {
        return new PartnershipResponse(
                r.partnershipId(),
                r.universityProfileId(),
                r.status(),
                r.requestedBy(),
                r.createdAt()
        );
    }
}