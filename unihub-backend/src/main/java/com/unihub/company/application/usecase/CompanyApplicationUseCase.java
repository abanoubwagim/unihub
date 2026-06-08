package com.unihub.company.application.usecase;

import com.unihub.company.api.dto.req.ReviewApplicationRequest;
import com.unihub.company.api.dto.res.ApplicationSummaryResponse;
import com.unihub.shared.api.dto.PageResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface CompanyApplicationUseCase {

    PageResponse<ApplicationSummaryResponse> getApplications(UUID userId, UUID jobPostingId, Pageable pageable);

    ApplicationSummaryResponse getApplication(UUID userId, UUID jobPostingId, UUID applicationId);

    ApplicationSummaryResponse review(UUID userId, UUID jobPostingId, UUID applicationId, ReviewApplicationRequest request);
}