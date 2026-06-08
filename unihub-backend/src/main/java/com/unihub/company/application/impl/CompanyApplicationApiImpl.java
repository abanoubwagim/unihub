package com.unihub.company.application.impl;

import com.unihub.company.api.dto.res.ApplicationSummaryResponse;
import com.unihub.company.domain.enums.ApplicationStatus;
import com.unihub.company.domain.enums.JobPostingStatus;
import com.unihub.company.domain.event.JobApplicationSubmittedEvent;
import com.unihub.company.domain.event.StudentHiredEvent;
import com.unihub.company.domain.event.StudentRejectedEvent;
import com.unihub.company.domain.model.JobApplication;
import com.unihub.company.domain.model.JobPosting;
import com.unihub.company.domain.repository.JobApplicationRepository;
import com.unihub.company.domain.repository.JobPostingRepository;
import com.unihub.shared.api.external.CompanyApplicationApi;
import com.unihub.shared.api.external.UniversityPartnershipApi;
import com.unihub.shared.domain.enums.JobType;
import com.unihub.shared.exception.InvalidOperationException;
import com.unihub.shared.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CompanyApplicationApiImpl implements CompanyApplicationApi {

    private final JobPostingRepository jobPostingRepository;
    private final JobApplicationRepository jobApplicationRepository;
    private final UniversityPartnershipApi universityPartnershipApi;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public void submitApplication(UUID jobPostingId,
                                  UUID studentProfileId,
                                  String cvUrl,
                                  UUID universityProfileId,
                                  boolean isCertVerified) {

        log.debug("Submitting application — jobPostingId={}, studentProfileId={}",
                jobPostingId, studentProfileId);

        // 1. Job posting must exist and be PUBLISHED
        JobPosting posting = jobPostingRepository.findById(jobPostingId)
                .orElseThrow(() -> new NotFoundException("Job posting not found"));

        if (posting.getStatus() != JobPostingStatus.PUBLISHED) {
            throw new InvalidOperationException(
                    "Applications can only be submitted to PUBLISHED job postings.");
        }

        // 2. Deadline check
        if (!LocalDate.now().isBefore(posting.getDeadline())) {
            throw new InvalidOperationException(
                    "The application deadline for this job posting has passed.");
        }

        // 3. Partnership check
        if (universityProfileId == null) {
            throw new InvalidOperationException(
                    "You must set your university first before applying to jobs.");
        }

        Set<UUID> partnerCompanyIds = universityPartnershipApi
                .getActivePartnerCompanyIds(universityProfileId);

        if (!partnerCompanyIds.contains(posting.getCompanyId())) {
            throw new InvalidOperationException(
                    "This company does not have an active partnership with your university.");
        }

        // 4. Job type restriction for non-graduated students
        if (!isCertVerified && posting.getJobType() == JobType.FULL_TIME) {
            throw new InvalidOperationException(
                    "Non-graduated students cannot apply to Full-time positions. " +
                            "Get your graduation certificate approved first.");
        }

        // 5. Duplicate application check
        if (jobApplicationRepository.existsByJobPostingIdAndStudentProfileId(
                jobPostingId, studentProfileId)) {
            throw new InvalidOperationException(
                    "You have already applied to this job posting.");
        }

        // 6. Save application and increment counter
        JobApplication application = JobApplication.builder()
                .jobPostingId(jobPostingId)
                .studentProfileId(studentProfileId)
                .cvUrl(cvUrl)
                .build();

        jobApplicationRepository.save(application);
        jobPostingRepository.incrementApplicantCount(jobPostingId);

        eventPublisher.publishEvent(
                new JobApplicationSubmittedEvent(
                        jobPostingId,
                        posting.getCompanyId(),
                        studentProfileId));

        log.info("Application submitted — jobPostingId={}, studentProfileId={}",
                jobPostingId, studentProfileId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<?> getMyApplications(UUID studentProfileId, Pageable pageable) {
        log.debug("Fetching applications for studentProfileId={}", studentProfileId);

        return jobApplicationRepository
                .findAllByStudentProfileId(studentProfileId, pageable)
                .map(this::toResponse);
    }

    @Override
    public void reviewApplication(UUID companyId, UUID applicationId, boolean accepted) {
        log.debug("API reviewApplication — companyId={}, applicationId={}, accepted={}",
                companyId, applicationId, accepted);

        JobApplication application = jobApplicationRepository
                .findByIdAndJobPosting_CompanyId(applicationId, companyId)
                .orElseThrow(() -> new NotFoundException("Application not found"));


        if (application.getStatus() != ApplicationStatus.PENDING) {
            throw new InvalidOperationException(
                    "Only PENDING applications can be reviewed.");
        }

        application.setStatus(accepted ? ApplicationStatus.ACCEPTED : ApplicationStatus.REJECTED);
        application.setReviewedAt(LocalDateTime.now());
        jobApplicationRepository.save(application);


        if (accepted) {
            eventPublisher.publishEvent(
                    new StudentHiredEvent(
                            application.getJobPostingId(),
                            companyId,
                            application.getStudentProfileId()));

            log.info("Application accepted via API — applicationId={}", applicationId);
        } else {
            eventPublisher.publishEvent(
                    new StudentRejectedEvent(
                            application.getJobPostingId(),
                            companyId,
                            application.getStudentProfileId(),
                            null)); // no rejection reason in cross-module API

            log.info("Application rejected via API — applicationId={}", applicationId);
        }
    }

    @Override
    public boolean hasStudentAlreadyApplied(UUID jobPostingId, UUID studentProfileId) {
        return jobApplicationRepository.hasStudentAlreadyApplied(jobPostingId, studentProfileId);
    }

    private ApplicationSummaryResponse toResponse(JobApplication a) {
        return new ApplicationSummaryResponse(
                a.getId(),
                a.getJobPostingId(),
                a.getStudentProfileId(),
                a.getCvUrl(),
                a.getStatus(),
                a.getRejectionReason(),
                a.getSubmittedAt(),
                a.getReviewedAt()
        );
    }
}