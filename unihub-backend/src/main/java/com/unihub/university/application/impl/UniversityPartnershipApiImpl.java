package com.unihub.university.application.impl;

import com.unihub.shared.api.dto.external.PartnershipRecord;
import com.unihub.shared.api.external.UniversityPartnershipApi;
import com.unihub.shared.domain.enums.PartnershipRequester;
import com.unihub.shared.domain.enums.PartnershipStatus;
import com.unihub.shared.exception.InvalidOperationException;
import com.unihub.shared.exception.NotFoundException;
import com.unihub.university.domain.event.UniversityPartnershipAcceptedEvent;
import com.unihub.university.domain.event.UniversityPartnershipRejectedEvent;
import com.unihub.university.domain.event.UniversityPartnershipRequestedEvent;
import com.unihub.university.domain.model.UniversityPartnership;
import com.unihub.university.domain.repository.UniversityPartnershipRepository;
import com.unihub.university.domain.repository.UniversityProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;


@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UniversityPartnershipApiImpl implements UniversityPartnershipApi {

    private final UniversityPartnershipRepository partnershipRepository;
    private final UniversityProfileRepository universityProfileRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public PartnershipRecord requestPartnership(UUID companyProfileId, UUID universityProfileId) {
        log.debug("Company requesting partnership — companyId={}, universityId={}",
                companyProfileId, universityProfileId);

        if (!universityProfileRepository.existsById(universityProfileId)) {
            throw new NotFoundException("University profile not found");
        }

        if (partnershipRepository.existsByUniversityIdAndCompanyId(
                universityProfileId, companyProfileId)) {
            throw new InvalidOperationException(
                    "A partnership with this university already exists or is pending.");
        }

        UniversityPartnership partnership = UniversityPartnership.builder()
                .universityId(universityProfileId)
                .companyId(companyProfileId)
                .status(PartnershipStatus.PENDING)
                .requestedBy(PartnershipRequester.COMPANY)
                .build();

        UniversityPartnership saved = partnershipRepository.save(partnership);
        log.info("Partnership requested by company — partnershipId={}", saved.getId());

        eventPublisher.publishEvent(
                new UniversityPartnershipRequestedEvent(saved.getId(), universityProfileId, companyProfileId));

        return toRecord(saved);
    }

    @Override
    public PartnershipRecord reviewPartnership(UUID companyProfileId, UUID partnershipId, boolean accept) {
        log.debug("Company reviewing partnership — companyId={}, partnershipId={}, accept={}",
                companyProfileId, partnershipId, accept);

        UniversityPartnership partnership = getPartnershipForCompany(partnershipId, companyProfileId);

        if (partnership.getStatus() != PartnershipStatus.PENDING) {
            throw new InvalidOperationException("Only PENDING partnerships can be reviewed.");
        }

        if (partnership.getRequestedBy() == PartnershipRequester.COMPANY) {
            throw new InvalidOperationException("You cannot review a partnership you requested.");
        }

        if (accept) {
            partnership.setStatus(PartnershipStatus.ACTIVE);
            partnershipRepository.save(partnership);
            log.info("Partnership accepted by company — partnershipId={}", partnershipId);
            eventPublisher.publishEvent(
                    new UniversityPartnershipAcceptedEvent(
                            partnership.getId(), partnership.getUniversityId(), companyProfileId));
        } else {
            partnership.setStatus(PartnershipStatus.REJECTED);
            partnershipRepository.save(partnership);
            log.info("Partnership rejected by company — partnershipId={}", partnershipId);
            eventPublisher.publishEvent(
                    new UniversityPartnershipRejectedEvent(
                            partnership.getId(), partnership.getUniversityId(), companyProfileId));
        }

        return toRecord(partnership);
    }

    @Override
    public void terminatePartnership(UUID companyProfileId, UUID partnershipId) {
        log.debug("Company terminating partnership — companyId={}, partnershipId={}",
                companyProfileId, partnershipId);

        UniversityPartnership partnership = getPartnershipForCompany(partnershipId, companyProfileId);

        if (partnership.getStatus() != PartnershipStatus.ACTIVE) {
            throw new InvalidOperationException("Only ACTIVE partnerships can be terminated.");
        }

        partnership.setStatus(PartnershipStatus.TERMINATED);
        partnershipRepository.save(partnership);
        log.info("Partnership terminated by company — partnershipId={}", partnershipId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PartnershipRecord> getAllForCompany(UUID companyProfileId, Pageable pageable) {
        return partnershipRepository
                .findAllByCompanyId(companyProfileId, pageable)
                .map(this::toRecord);
    }

    @Override
    @Transactional(readOnly = true)
    public Set<UUID> getActivePartnerCompanyIds(UUID universityProfileId) {
        return new HashSet<>(partnershipRepository.findActivePartnerCompanyIds(universityProfileId));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isMajorOfferedByUniversity(UUID universityId, UUID majorId) {
        return universityProfileRepository.existsByIdAndMajors_Id(universityId, majorId);
    }


    private UniversityPartnership getPartnershipForCompany(UUID partnershipId, UUID companyProfileId) {
        return partnershipRepository.findByIdAndCompanyId(partnershipId, companyProfileId)
                .orElseThrow(() -> new NotFoundException("Partnership not found"));
    }

    private PartnershipRecord toRecord(UniversityPartnership p) {
        return new PartnershipRecord(
                p.getId(),
                p.getUniversityId(),
                p.getCompanyId(),
                p.getStatus(),
                p.getRequestedBy(),
                p.getCreatedAt()
        );
    }
}