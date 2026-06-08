package com.unihub.company.infrastructure.persistence.jpa;

import com.unihub.company.domain.model.JobApplication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface JpaJobApplicationRepository extends JpaRepository<JobApplication, UUID> {

    Page<JobApplication> findAllByJobPostingId(UUID jobPostingId, Pageable pageable);

    boolean existsByJobPostingIdAndStudentProfileId(UUID jobPostingId, UUID studentProfileId);

    @Query("""
            SELECT a FROM JobApplication a
            JOIN JobPosting p ON a.jobPostingId = p.id
            WHERE a.id = :applicationId AND p.companyId = :companyId
            """)
    Optional<JobApplication> findByIdAndJobPosting_CompanyId(
            @Param("applicationId") UUID applicationId,
            @Param("companyId") UUID companyId);
}