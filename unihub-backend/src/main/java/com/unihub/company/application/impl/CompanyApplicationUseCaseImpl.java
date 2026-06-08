package com.unihub.company.application.impl;

import com.unihub.company.api.dto.req.ReviewApplicationRequest;
import com.unihub.company.api.dto.res.ApplicationSummaryResponse;
import com.unihub.company.application.usecase.CompanyApplicationUseCase;
import com.unihub.company.domain.enums.ApplicationStatus;
import com.unihub.company.domain.event.StudentHiredEvent;
import com.unihub.company.domain.event.StudentRejectedEvent;
import com.unihub.company.domain.model.CompanyProfile;
import com.unihub.company.domain.model.JobApplication;
import com.unihub.company.domain.repository.CompanyProfileRepository;
import com.unihub.company.domain.repository.JobApplicationRepository;
import com.unihub.company.domain.repository.JobPostingRepository;
import com.unihub.shared.api.dto.PageResponse;
import com.unihub.shared.exception.InvalidOperationException;
import com.unihub.shared.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CompanyApplicationUseCaseImpl implements CompanyApplicationUseCase {

    private final CompanyProfileRepository companyProfileRepository;
    private final JobApplicationRepository jobApplicationRepository;
    private final JobPostingRepository jobPostingRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ApplicationSummaryResponse> getApplications(UUID userId, UUID jobPostingId, Pageable pageable) {
        getProfileByUserId(userId); // ownership assertion
        jobPostingRepository.findByIdAndCompanyId(jobPostingId, userId)
                .orElseThrow(() -> new NotFoundException("Job posting not found"));

        return PageResponse.from(
                jobApplicationRepository.findAllByJobPostingId(jobPostingId, pageable)
                        .map(this::toResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public ApplicationSummaryResponse getApplication(UUID userId, UUID jobPostingId, UUID applicationId) {
        CompanyProfile profile = getProfileByUserId(userId);
        JobApplication application = jobApplicationRepository
                .findByIdAndJobPosting_CompanyId(applicationId, profile.getId())
                .orElseThrow(() -> new NotFoundException("Application not found"));
        return toResponse(application);
    }

    @Override
    public ApplicationSummaryResponse review(UUID userId, UUID jobPostingId, UUID applicationId,
                                             ReviewApplicationRequest request) {
        log.debug("Reviewing application — userId={}, applicationId={}, accepted={}",
                userId, applicationId, request.accepted());

        CompanyProfile profile = getProfileByUserId(userId);
        JobApplication application = jobApplicationRepository
                .findByIdAndJobPosting_CompanyId(applicationId, profile.getId())
                .orElseThrow(() -> new NotFoundException("Application not found"));

        if (application.getStatus() != ApplicationStatus.PENDING) {
            throw new InvalidOperationException("Only PENDING applications can be reviewed.");
        }

        if (request.accepted()) {
            application.setStatus(ApplicationStatus.ACCEPTED);
            application.setReviewedAt(LocalDateTime.now());
            jobApplicationRepository.save(application);

            log.info("Application accepted — applicationId={}, studentProfileId={}",
                    applicationId, application.getStudentProfileId());

            eventPublisher.publishEvent(
                    new StudentHiredEvent(
                            application.getJobPostingId(),
                            profile.getId(),
                            application.getStudentProfileId()));
        } else {
            application.setStatus(ApplicationStatus.REJECTED);
            application.setRejectionReason(request.rejectionReason());
            application.setReviewedAt(LocalDateTime.now());
            jobApplicationRepository.save(application);

            log.info("Application rejected — applicationId={}, studentProfileId={}",
                    applicationId, application.getStudentProfileId());

            eventPublisher.publishEvent(
                    new StudentRejectedEvent(
                            application.getJobPostingId(),
                            profile.getId(),
                            application.getStudentProfileId(),
                            request.rejectionReason()));
        }

        return toResponse(application);
    }

    private CompanyProfile getProfileByUserId(UUID userId) {
        return companyProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("Company profile not found"));
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