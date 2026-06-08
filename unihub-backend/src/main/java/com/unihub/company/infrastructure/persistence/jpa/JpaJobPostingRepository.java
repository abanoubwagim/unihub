package com.unihub.company.infrastructure.persistence.jpa;

import com.unihub.company.domain.enums.JobPostingStatus;
import com.unihub.company.domain.model.JobPosting;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface JpaJobPostingRepository extends JpaRepository<JobPosting, UUID> {

    Optional<JobPosting> findByIdAndCompanyId(UUID id, UUID companyId);

    Page<JobPosting> findAllByCompanyId(UUID companyId, Pageable pageable);

    Page<JobPosting> findAllByCompanyIdAndStatus(UUID companyId, JobPostingStatus status, Pageable pageable);

    @Query("SELECT j FROM JobPosting j WHERE j.status = 'PUBLISHED' AND j.deadline < :date")
    List<JobPosting> findAllExpired(@Param("date") LocalDate date);

    @Modifying
    @Query("UPDATE JobPosting j SET j.applicantCount = j.applicantCount + 1 WHERE j.id = :id")
    void incrementApplicantCount(@Param("id") UUID id);

    Page<JobPosting> findAllByCompanyIdInAndStatus(
            Set<UUID> companyIds, JobPostingStatus status, Pageable pageable);

    Optional<JobPosting> findByIdAndStatus(UUID id, JobPostingStatus status);
}