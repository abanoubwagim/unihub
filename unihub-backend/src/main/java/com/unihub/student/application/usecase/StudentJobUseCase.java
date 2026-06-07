package com.unihub.student.application.usecase;

import com.unihub.company.api.dto.external.JobPostingPublicInfo;
import com.unihub.shared.api.dto.PageResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface StudentJobUseCase {

    PageResponse<JobPostingPublicInfo> getAvailableJobs(UUID userId, Pageable pageable);

    JobPostingPublicInfo getJobDetail(UUID userId, UUID jobPostingId);

    void applyToJob(UUID userId, UUID jobPostingId, MultipartFile cvFile);
}