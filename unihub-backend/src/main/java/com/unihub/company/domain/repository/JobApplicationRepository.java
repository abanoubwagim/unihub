package com.unihub.company.domain.repository;

import com.unihub.company.domain.model.JobApplication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface JobApplicationRepository {

    Optional<JobApplication> findById(UUID id);

    Optional<JobApplication> findByIdAndJobPosting_CompanyId(UUID applicationId, UUID companyId);

    Page<JobApplication> findAllByJobPostingId(UUID jobPostingId, Pageable pageable);

    JobApplication save(JobApplication application);

    // internal use within company module
    boolean existsByJobPostingIdAndStudentProfileId(UUID jobPostingId, UUID studentProfileId);

    // exposed semantically for cross-module use via CompanyApplicationApi
    boolean hasStudentAlreadyApplied(UUID jobPostingId, UUID studentProfileId);

    Page<JobApplication> findAllByStudentProfileId(UUID studentProfileId, Pageable pageable);
}