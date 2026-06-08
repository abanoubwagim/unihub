package com.unihub.company.infrastructure.persistence.impl;

import com.unihub.company.domain.model.JobApplication;
import com.unihub.company.domain.repository.JobApplicationRepository;
import com.unihub.company.infrastructure.persistence.jpa.JpaJobApplicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class JobApplicationRepositoryImpl implements JobApplicationRepository {

    private final JpaJobApplicationRepository jpa;

    @Override
    public Optional<JobApplication> findById(UUID id) {
        return jpa.findById(id);
    }

    @Override
    public Optional<JobApplication> findByIdAndJobPosting_CompanyId(UUID applicationId, UUID companyId) {
        return jpa.findByIdAndJobPosting_CompanyId(applicationId, companyId);
    }

    @Override
    public Page<JobApplication> findAllByJobPostingId(UUID jobPostingId, Pageable pageable) {
        return jpa.findAllByJobPostingId(jobPostingId, pageable);
    }

    @Override
    public boolean existsByJobPostingIdAndStudentProfileId(UUID jobPostingId, UUID studentProfileId) {
        return jpa.existsByJobPostingIdAndStudentProfileId(jobPostingId, studentProfileId);
    }

    @Override
    public JobApplication save(JobApplication application) {
        return jpa.save(application);
    }

    @Override
    public boolean hasStudentAlreadyApplied(UUID jobPostingId, UUID studentProfileId) {
        return jpa.existsByJobPostingIdAndStudentProfileId(jobPostingId, studentProfileId);
    }

    @Override
    public Page<JobApplication> findAllByStudentProfileId(UUID studentProfileId, Pageable pageable) {
        return jpa.findAllByJobPostingId(studentProfileId, pageable);
    }
}