package com.unihub.company.application.usecase;

import com.unihub.company.api.dto.req.CreateJobPostingRequest;
import com.unihub.company.api.dto.req.UpdateJobPostingRequest;
import com.unihub.company.api.dto.res.JobPostingResponse;
import com.unihub.company.api.dto.res.JobPostingSummaryResponse;
import com.unihub.company.domain.enums.JobPostingStatus;
import com.unihub.shared.api.dto.PageResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface CompanyJobPostingUseCase {

    JobPostingResponse createDraft(UUID userId, CreateJobPostingRequest request);

    JobPostingResponse updateDraft(UUID userId, UUID postingId, UpdateJobPostingRequest request);

    JobPostingResponse publish(UUID userId, UUID postingId);

    JobPostingResponse close(UUID userId, UUID postingId);

    PageResponse<JobPostingSummaryResponse> getAll(UUID userId, JobPostingStatus status, Pageable pageable);

    JobPostingResponse getById(UUID userId, UUID postingId);

    void delete(UUID userId, UUID postingId);
}