package com.unihub.company.application.impl;


import com.unihub.company.api.dto.external.JobPostingPublicInfo;
import com.unihub.company.domain.enums.JobPostingStatus;
import com.unihub.company.domain.model.JobPosting;
import com.unihub.company.infrastructure.persistence.jpa.JpaJobPostingRepository;
import com.unihub.shared.api.external.CompanyJobPublicApi;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompanyJobPublicApiImpl implements CompanyJobPublicApi {

    private final JpaJobPostingRepository jpa;

    @Override
    public Page<JobPostingPublicInfo> getPublishedJobsByCompanyIds(Set<UUID> companyIds, Pageable pageable) {
        if (companyIds == null || companyIds.isEmpty()) {
            return Page.empty(pageable);
        }
        return jpa.findAllByCompanyIdInAndStatus(companyIds, JobPostingStatus.PUBLISHED, pageable)
                .map(this::toInfo);
    }

    @Override
    public Optional<JobPostingPublicInfo> getPublishedJobById(UUID jobPostingId) {
        return jpa.findByIdAndStatus(jobPostingId, JobPostingStatus.PUBLISHED)
                .map(this::toInfo);
    }

    private JobPostingPublicInfo toInfo(JobPosting p) {
        return new JobPostingPublicInfo(
                p.getId(),
                p.getCompanyId(),
                p.getTitle(),
                p.getJobType(),
                p.getWorkLocationType(),
                p.getSalaryFrom(),
                p.getSalaryTo(),
                p.getDescription(),
                p.getDeadline(),
                p.getApplicantCount(),
                p.getPublishedAt()
        );
    }
}