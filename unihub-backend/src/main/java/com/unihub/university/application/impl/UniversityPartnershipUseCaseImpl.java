package com.unihub.university.application.impl;

import com.unihub.shared.api.dto.PageResponse;
import com.unihub.shared.domain.enums.PartnershipRequester;
import com.unihub.shared.domain.enums.PartnershipStatus;
import com.unihub.shared.exception.InvalidOperationException;
import com.unihub.shared.exception.NotFoundException;
import com.unihub.university.api.dto.req.CreatePartnershipRequest;
import com.unihub.university.api.dto.req.ReviewPartnershipRequest;
import com.unihub.university.api.dto.res.PartnershipResponse;
import com.unihub.university.application.usecase.UniversityPartnershipUseCase;
import com.unihub.university.domain.event.UniversityPartnershipAcceptedEvent;
import com.unihub.university.domain.event.UniversityPartnershipRejectedEvent;
import com.unihub.university.domain.event.UniversityPartnershipRequestedEvent;
import com.unihub.university.domain.model.UniversityPartnership;
import com.unihub.university.domain.model.UniversityProfile;
import com.unihub.university.domain.repository.UniversityPartnershipRepository;
import com.unihub.university.domain.repository.UniversityProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UniversityPartnershipUseCaseImpl implements UniversityPartnershipUseCase {

    private final UniversityProfileRepository universityProfileRepository;
    private final UniversityPartnershipRepository partnershipRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PartnershipResponse> getAll(UUID userId, Pageable pageable) {
        UniversityProfile profile = getProfileByUserId(userId);
        return PageResponse.from(
                partnershipRepository.findAllByUniversityId(profile.getId(), pageable)
                        .map(this::toResponse));
    }

    @Override
    public PartnershipResponse requestPartnership(UUID userId, CreatePartnershipRequest request) {
        log.debug("University requesting partnership — userId={}, companyId={}", userId, request.companyId());

        UniversityProfile profile = getProfileByUserId(userId);

        if (partnershipRepository.existsByUniversityIdAndCompanyId(
                profile.getId(), request.companyId())) {
            throw new InvalidOperationException("A partnership with this company already exists or is pending.");
        }

        UniversityPartnership partnership = UniversityPartnership.builder()
                .universityId(profile.getId())
                .companyId(request.companyId())
                .status(PartnershipStatus.PENDING)
                .requestedBy(PartnershipRequester.UNIVERSITY)
                .build();

        UniversityPartnership saved = partnershipRepository.save(partnership);
        log.info("Partnership requested — universityId={}, companyId={}, partnershipId={}",
                profile.getId(), request.companyId(), saved.getId());

        eventPublisher.publishEvent(
                new UniversityPartnershipRequestedEvent(saved.getId(), profile.getId(), request.companyId()));

        return toResponse(saved);
    }

    @Override
    public PartnershipResponse reviewPartnership(UUID userId, UUID partnershipId, ReviewPartnershipRequest request) {
        log.debug("Reviewing partnership — userId={}, partnershipId={}, accept={}", userId, partnershipId, request.accept());

        UniversityProfile profile = getProfileByUserId(userId);
        UniversityPartnership partnership = getOwnedPartnership(partnershipId, profile.getId());

        if (partnership.getStatus() != PartnershipStatus.PENDING) {
            throw new InvalidOperationException("Only pending partnerships can be reviewed.");
        }

        if (partnership.getRequestedBy() == PartnershipRequester.UNIVERSITY) {
            throw new InvalidOperationException("You cannot review a partnership you requested.");
        }

        if (request.accept()) {
            partnership.setStatus(PartnershipStatus.ACTIVE);
            partnershipRepository.save(partnership);
            log.info("Partnership accepted — partnershipId={}", partnershipId);
            eventPublisher.publishEvent(
                    new UniversityPartnershipAcceptedEvent(partnership.getId(), profile.getId(), partnership.getCompanyId()));
        } else {
            partnership.setStatus(PartnershipStatus.REJECTED);
            partnershipRepository.save(partnership);
            log.info("Partnership rejected — partnershipId={}", partnershipId);
            eventPublisher.publishEvent(
                    new UniversityPartnershipRejectedEvent(partnership.getId(), profile.getId(), partnership.getCompanyId()));
        }

        return toResponse(partnership);
    }

    @Override
    public void terminatePartnership(UUID userId, UUID partnershipId) {
        log.debug("Terminating partnership — userId={}, partnershipId={}", userId, partnershipId);

        UniversityProfile profile = getProfileByUserId(userId);
        UniversityPartnership partnership = getOwnedPartnership(partnershipId, profile.getId());

        if (partnership.getStatus() != PartnershipStatus.ACTIVE) {
            throw new InvalidOperationException("Only active partnerships can be terminated.");
        }

        partnership.setStatus(PartnershipStatus.TERMINATED);
        partnershipRepository.save(partnership);
        log.info("Partnership terminated — partnershipId={}", partnershipId);
    }


    private UniversityPartnership getOwnedPartnership(UUID partnershipId, UUID universityProfileId) {
        return partnershipRepository.findByIdAndUniversityId(partnershipId, universityProfileId)
                .orElseThrow(() -> new NotFoundException("Partnership not found"));
    }

    private UniversityProfile getProfileByUserId(UUID userId) {
        return universityProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("University profile not found"));
    }

    private PartnershipResponse toResponse(UniversityPartnership p) {
        return new PartnershipResponse(
                p.getId(),
                p.getCompanyId(),
                p.getStatus(),
                p.getRequestedBy(),
                0, // hiredStudentCount — will be populated from CompanyPublicApi when company module is built
                p.getCreatedAt());
    }
}