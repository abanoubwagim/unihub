package com.unihub.shared.api.external;

import com.unihub.company.api.dto.external.JobPostingPublicInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface CompanyJobPublicApi {

    Page<JobPostingPublicInfo> getPublishedJobsByCompanyIds(Set<UUID> companyIds, Pageable pageable);

    Optional<JobPostingPublicInfo> getPublishedJobById(UUID jobPostingId);
}