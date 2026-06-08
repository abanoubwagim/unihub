package com.unihub.company.application.impl;

import com.unihub.company.api.dto.req.CreateJobPostingRequest;
import com.unihub.company.api.dto.req.UpdateJobPostingRequest;
import com.unihub.company.api.dto.res.JobPostingResponse;
import com.unihub.company.api.dto.res.JobPostingSummaryResponse;
import com.unihub.company.application.usecase.CompanyJobPostingUseCase;
import com.unihub.company.domain.enums.JobPostingStatus;
import com.unihub.company.domain.model.CompanyProfile;
import com.unihub.company.domain.model.JobPosting;
import com.unihub.company.domain.repository.CompanyProfileRepository;
import com.unihub.company.domain.repository.JobPostingRepository;
import com.unihub.shared.api.dto.PageResponse;
import com.unihub.shared.exception.InvalidOperationException;
import com.unihub.shared.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CompanyJobPostingUseCaseImpl implements CompanyJobPostingUseCase {

    private final CompanyProfileRepository companyProfileRepository;
    private final JobPostingRepository jobPostingRepository;

    @Override
    public JobPostingResponse createDraft(UUID userId, CreateJobPostingRequest request) {
        log.debug("Creating job posting draft for userId={}", userId);

        CompanyProfile profile = getProfileByUserId(userId);

        JobPosting posting = JobPosting.builder()
                .companyId(profile.getId())
                .title(request.title())
                .jobType(request.jobType())
                .workLocationType(request.workLocationType())
                .salaryFrom(request.salaryFrom())
                .salaryTo(request.salaryTo())
                .description(request.description())
                .deadline(request.deadline())
                .status(JobPostingStatus.DRAFT)
                .createdAt(LocalDateTime.now())
                .build();

        JobPosting saved = jobPostingRepository.save(posting);

        if (request.publishNow()) {
            validateForPublish(posting);
            posting.setStatus(JobPostingStatus.PUBLISHED);
            posting.setPublishedAt(LocalDateTime.now());
            saved = jobPostingRepository.save(posting);
            log.info("Job posting created and published — userId={}, postingId={}", userId, saved.getId());
        } else {
            log.info("Job posting saved as draft — userId={}, postingId={}", userId, saved.getId());
        }
        return toResponse(saved);
    }

    @Override
    public JobPostingResponse updateDraft(UUID userId, UUID postingId, UpdateJobPostingRequest request) {
        log.debug("Updating job posting draft — userId={}, postingId={}", userId, postingId);

        CompanyProfile profile = getProfileByUserId(userId);
        JobPosting posting = getOwnedPosting(postingId, profile.getId());

        if (posting.getStatus() != JobPostingStatus.DRAFT) {
            throw new InvalidOperationException("Only Draft postings can be updated.");
        }

        if (request.title() != null) posting.setTitle(request.title());
        if (request.jobType() != null) posting.setJobType(request.jobType());
        if (request.workLocationType() != null) posting.setWorkLocationType(request.workLocationType());
        if (request.salaryFrom() != null) posting.setSalaryFrom(request.salaryFrom());
        if (request.salaryTo() != null) posting.setSalaryTo(request.salaryTo());
        if (request.description() != null) posting.setDescription(request.description());
        if (request.deadline() != null) posting.setDeadline(request.deadline());

        JobPosting saved = jobPostingRepository.save(posting);
        log.info("Job posting draft updated — userId={}, postingId={}", userId, postingId);
        return toResponse(saved);
    }

    @Override
    public JobPostingResponse publish(UUID userId, UUID postingId) {
        log.debug("Publishing job posting — userId={}, postingId={}", userId, postingId);

        CompanyProfile profile = getProfileByUserId(userId);
        JobPosting posting = getOwnedPosting(postingId, profile.getId());

        if (posting.getStatus() != JobPostingStatus.DRAFT) {
            throw new InvalidOperationException("Only Draft postings can be published.");
        }

        validateForPublish(posting);

        posting.setStatus(JobPostingStatus.PUBLISHED);
        posting.setPublishedAt(LocalDateTime.now());

        JobPosting saved = jobPostingRepository.save(posting);
        log.info("Job posting published — userId={}, postingId={}", userId, postingId);
        return toResponse(saved);
    }

    @Override
    public JobPostingResponse close(UUID userId, UUID postingId) {
        log.debug("Closing job posting — userId={}, postingId={}", userId, postingId);

        CompanyProfile profile = getProfileByUserId(userId);
        JobPosting posting = getOwnedPosting(postingId, profile.getId());

        if (posting.getStatus() != JobPostingStatus.PUBLISHED) {
            throw new InvalidOperationException("Only PUBLISHED postings can be closed.");
        }

        posting.setStatus(JobPostingStatus.CLOSED);
        JobPosting saved = jobPostingRepository.save(posting);
        log.info("Job posting closed — userId={}, postingId={}", userId, postingId);
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<JobPostingSummaryResponse> getAll(UUID userId, JobPostingStatus status, Pageable pageable) {
        CompanyProfile profile = getProfileByUserId(userId);

        if (status != null) {
            return PageResponse.from(
                    jobPostingRepository.findAllByCompanyIdAndStatus(profile.getId(), status, pageable)
                            .map(this::toSummaryResponse));
        }
        return PageResponse.from(
                jobPostingRepository.findAllByCompanyId(profile.getId(), pageable)
                        .map(this::toSummaryResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public JobPostingResponse getById(UUID userId, UUID postingId) {
        CompanyProfile profile = getProfileByUserId(userId);
        return toResponse(getOwnedPosting(postingId, profile.getId()));
    }

    @Override
    public void delete(UUID userId, UUID postingId) {
        log.debug("Deleting job posting — userId={}, postingId={}", userId, postingId);

        CompanyProfile profile = getProfileByUserId(userId);
        JobPosting posting = getOwnedPosting(postingId, profile.getId());

        if (posting.getStatus() != JobPostingStatus.DRAFT) {
            throw new InvalidOperationException("Only DRAFT postings can be deleted.");
        }

        jobPostingRepository.delete(posting);
        log.info("Job posting deleted — userId={}, postingId={}", userId, postingId);
    }

    private void validateForPublish(JobPosting posting) {
        if (posting.getTitle() == null || posting.getTitle().isBlank())
            throw new InvalidOperationException("Title is required to publish a job posting.");
        if (posting.getJobType() == null)
            throw new InvalidOperationException("Job type is required to publish a job posting.");
        if (posting.getWorkLocationType() == null)
            throw new InvalidOperationException("Work location type is required to publish a job posting.");
        if (posting.getDescription() == null || posting.getDescription().isBlank())
            throw new InvalidOperationException("Description is required to publish a job posting.");
        if (posting.getDeadline() == null)
            throw new InvalidOperationException("Deadline is required to publish a job posting.");
        if (!posting.getDeadline().isAfter(LocalDate.now()))
            throw new InvalidOperationException("Deadline must be a future date.");
        if (posting.getSalaryFrom() != null && posting.getSalaryTo() != null
                && posting.getSalaryTo().compareTo(posting.getSalaryFrom()) < 0) {
            throw new InvalidOperationException("salaryTo must be greater than or equal to salaryFrom.");
        }
    }

    private JobPosting getOwnedPosting(UUID postingId, UUID companyId) {
        return jobPostingRepository.findByIdAndCompanyId(postingId, companyId)
                .orElseThrow(() -> new NotFoundException("Job posting not found"));
    }

    private CompanyProfile getProfileByUserId(UUID userId) {
        return companyProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("Company profile not found"));
    }

    private JobPostingResponse toResponse(JobPosting p) {
        return new JobPostingResponse(
                p.getId(),
                p.getCompanyId(),
                p.getTitle(),
                p.getJobType(),
                p.getWorkLocationType(),
                p.getSalaryFrom(),
                p.getSalaryTo(),
                p.getDescription(),
                p.getDeadline(),
                p.getStatus(),
                p.getApplicantCount(),
                p.getPublishedAt(),
                p.getCreatedAt()
        );
    }

    private JobPostingSummaryResponse toSummaryResponse(JobPosting p) {
        return new JobPostingSummaryResponse(
                p.getId(),
                p.getTitle(),
                p.getJobType(),
                p.getWorkLocationType(),
                p.getStatus(),
                p.getApplicantCount(),
                p.getDeadline(),
                p.getCreatedAt()
        );
    }
}