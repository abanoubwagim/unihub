package com.unihub.student.application.impl;


import com.unihub.company.api.dto.external.JobPostingPublicInfo;
import com.unihub.shared.api.dto.PageResponse;
import com.unihub.shared.api.external.CompanyApplicationApi;
import com.unihub.shared.api.external.CompanyJobPublicApi;
import com.unihub.shared.api.external.UniversityPartnershipApi;
import com.unihub.shared.exception.InvalidOperationException;
import com.unihub.shared.exception.NotFoundException;
import com.unihub.shared.storage.FileStorageService;
import com.unihub.student.application.usecase.StudentJobUseCase;
import com.unihub.student.domain.model.StudentProfile;
import com.unihub.student.domain.repository.StudentProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class StudentJobUseCaseImpl implements StudentJobUseCase {

    private final StudentProfileRepository studentProfileRepository;
    private final UniversityPartnershipApi universityPartnershipApi;
    private final CompanyJobPublicApi companyJobPublicApi;
    private final CompanyApplicationApi companyApplicationApi;
    private final FileStorageService fileStorageService;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<JobPostingPublicInfo> getAvailableJobs(UUID userId, Pageable pageable) {
        log.debug("Fetching available jobs — userId={}", userId);

        StudentProfile profile = getProfileByUserId(userId);

        if (profile.getUniversityId() == null) {
            log.warn("Student has no university set — userId={}", userId);
            return PageResponse.from(Page.empty(pageable));
        }

        Set<UUID> partnerCompanyIds = universityPartnershipApi
                .getActivePartnerCompanyIds(profile.getUniversityId());

        if (partnerCompanyIds.isEmpty()) {
            log.info("No partner companies found for university — universityId={}", profile.getUniversityId());
            return PageResponse.from(Page.empty(pageable));
        }

        log.debug("Found {} partner companies — universityId={}", partnerCompanyIds.size(), profile.getUniversityId());
        return PageResponse.from(
                companyJobPublicApi.getPublishedJobsByCompanyIds(partnerCompanyIds, pageable));
    }

    @Override
    @Transactional(readOnly = true)
    public JobPostingPublicInfo getJobDetail(UUID userId, UUID jobPostingId) {
        log.debug("Fetching job detail — userId={}, jobPostingId={}", userId, jobPostingId);

        StudentProfile profile = getProfileByUserId(userId);

        if (profile.getUniversityId() == null) {
            log.warn("Student has no university — userId={}", userId);
            throw new InvalidOperationException(
                    "You must set your university first.");
        }

        Set<UUID> partnerCompanyIds = universityPartnershipApi
                .getActivePartnerCompanyIds(profile.getUniversityId());

        JobPostingPublicInfo job = companyJobPublicApi.getPublishedJobById(jobPostingId)
                .orElseThrow(() -> {
                    log.warn("Job posting not found — jobPostingId={}", jobPostingId);
                    return new NotFoundException("Job posting not found");
                });

        if (!partnerCompanyIds.contains(job.companyId())) {
            log.warn("Job not available for student's university — userId={}, jobPostingId={}, companyId={}",
                    userId, jobPostingId, job.companyId());
            throw new InvalidOperationException(
                    "This job posting is not available for students of your university.");
        }

        log.debug("Job detail fetched successfully — userId={}, jobPostingId={}", userId, jobPostingId);
        return job;
    }

    @Override
    @Transactional
    public void applyToJob(UUID userId, UUID jobPostingId, MultipartFile cvFile) {
        log.debug("Student applying to job — userId={}, jobPostingId={}", userId, jobPostingId);

        StudentProfile profile = getProfileByUserId(userId);

        if (profile.getUniversityId() == null) {
            throw new InvalidOperationException(
                    "You must set your university before applying to jobs.");
        }

        Set<UUID> partnerCompanyIds = universityPartnershipApi
                .getActivePartnerCompanyIds(profile.getUniversityId());

        JobPostingPublicInfo job = companyJobPublicApi
                .getPublishedJobById(jobPostingId)
                .orElseThrow(() -> {
                    log.warn("Job posting not found — jobPostingId={}", jobPostingId);
                    return new NotFoundException("Job posting not found");
                });

        if (!partnerCompanyIds.contains(job.companyId())) {
            log.warn("Company not partnered with student's university — userId={}, companyId={}",
                    userId, job.companyId());
            throw new InvalidOperationException(
                    "This company does not have an active partnership with your university.");
        }

        if (cvFile == null || cvFile.isEmpty()) {
            log.warn("CV file missing in application — userId={}, jobPostingId={}", userId, jobPostingId);
            throw new InvalidOperationException("CV file is required to apply.");
        }


        if (companyApplicationApi.hasStudentAlreadyApplied(
                jobPostingId, profile.getId())) {
            log.warn("Duplicate application attempt — userId={}, jobPostingId={}", userId, jobPostingId);
            throw new InvalidOperationException("You have already applied to this job posting.");
        }

        String cvUrl = fileStorageService.upload(
                cvFile, "students/cvs/" + profile.getId() + "/" + jobPostingId);

        log.debug("CV uploaded successfully — userId={}, cvUrl={}", userId, cvUrl);

        companyApplicationApi.submitApplication(
                jobPostingId,
                profile.getId(),
                cvUrl,
                profile.getUniversityId(),
                profile.isCertVerified()
        );

        log.info("Application submitted — userId={}, jobPostingId={}", userId, jobPostingId);
    }

    private StudentProfile getProfileByUserId(UUID userId) {
        return studentProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("Student profile not found"));
    }
}