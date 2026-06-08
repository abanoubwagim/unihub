package com.unihub.company.domain.repository;

import com.unihub.company.domain.enums.JobPostingStatus;
import com.unihub.company.domain.model.JobPosting;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JobPostingRepository {

    Optional<JobPosting> findById(UUID id);

    Optional<JobPosting> findByIdAndCompanyId(UUID id, UUID companyId);

    Page<JobPosting> findAllByCompanyId(UUID companyId, Pageable pageable);

    Page<JobPosting> findAllByCompanyIdAndStatus(UUID companyId, JobPostingStatus status, Pageable pageable);

    List<JobPosting> findAllExpired(LocalDate date);

    JobPosting save(JobPosting posting);

    void incrementApplicantCount(UUID id);

    void delete(JobPosting posting);
}