package com.unihub.company.infrastructure.persistence.impl;

import com.unihub.company.domain.enums.JobPostingStatus;
import com.unihub.company.domain.model.JobPosting;
import com.unihub.company.domain.repository.JobPostingRepository;
import com.unihub.company.infrastructure.persistence.jpa.JpaJobPostingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class JobPostingRepositoryImpl implements JobPostingRepository {

    private final JpaJobPostingRepository jpa;

    @Override
    public Optional<JobPosting> findById(UUID id) {
        return jpa.findById(id);
    }

    @Override
    public Optional<JobPosting> findByIdAndCompanyId(UUID id, UUID companyId) {
        return jpa.findByIdAndCompanyId(id, companyId);
    }

    @Override
    public Page<JobPosting> findAllByCompanyId(UUID companyId, Pageable pageable) {
        return jpa.findAllByCompanyId(companyId, pageable);
    }

    @Override
    public Page<JobPosting> findAllByCompanyIdAndStatus(UUID companyId, JobPostingStatus status, Pageable pageable) {
        return jpa.findAllByCompanyIdAndStatus(companyId, status, pageable);
    }

    @Override
    public List<JobPosting> findAllExpired(LocalDate date) {
        return jpa.findAllExpired(date);
    }

    @Override
    public JobPosting save(JobPosting posting) {
        return jpa.save(posting);
    }

    @Override
    public void incrementApplicantCount(UUID id) {
        jpa.incrementApplicantCount(id);
    }

    @Override
    public void delete(JobPosting posting) {
        jpa.delete(posting);
    }
}