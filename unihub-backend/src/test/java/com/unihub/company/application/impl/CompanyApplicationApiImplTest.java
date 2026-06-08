package com.unihub.company.application.impl;

import com.unihub.company.domain.enums.JobPostingStatus;
import com.unihub.company.domain.model.JobApplication;
import com.unihub.company.domain.model.JobPosting;
import com.unihub.company.domain.repository.JobApplicationRepository;
import com.unihub.company.domain.repository.JobPostingRepository;
import com.unihub.shared.api.external.UniversityPartnershipApi;
import com.unihub.shared.domain.enums.JobType;
import com.unihub.shared.exception.InvalidOperationException;
import com.unihub.shared.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CompanyApplicationApi (submitApplication) Tests")
class CompanyApplicationApiImplTest {

    private final UUID jobPostingId = UUID.randomUUID();
    private final UUID studentProfileId = UUID.randomUUID();
    private final UUID companyId = UUID.randomUUID();
    private final UUID universityId = UUID.randomUUID();

    @Mock
    private JobPostingRepository jobPostingRepository;

    @Mock
    private JobApplicationRepository jobApplicationRepository;

    @Mock
    private UniversityPartnershipApi universityPartnershipApi;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private CompanyApplicationApiImpl applicationApi;

    private JobPosting publishedPosting;

    @BeforeEach
    void setUp() {
        publishedPosting = buildPosting(JobPostingStatus.PUBLISHED, JobType.INTERNSHIP,
                LocalDate.now().plusDays(30));
    }


    @Test
    @DisplayName("should submit application successfully when all conditions are met")
    void shouldSubmitApplicationSuccessfully() {
        when(jobPostingRepository.findById(jobPostingId)).thenReturn(Optional.of(publishedPosting));
        when(universityPartnershipApi.getActivePartnerCompanyIds(universityId))
                .thenReturn(Set.of(companyId));
        when(jobApplicationRepository.existsByJobPostingIdAndStudentProfileId(
                jobPostingId, studentProfileId)).thenReturn(false);
        when(jobApplicationRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        assertThatNoException().isThrownBy(() ->
                applicationApi.submitApplication(jobPostingId, studentProfileId,
                        "https://cv.pdf", universityId, false));

        ArgumentCaptor<JobApplication> captor = ArgumentCaptor.forClass(JobApplication.class);
        verify(jobApplicationRepository).save(captor.capture());
        assertThat(captor.getValue().getJobPostingId()).isEqualTo(jobPostingId);
        assertThat(captor.getValue().getStudentProfileId()).isEqualTo(studentProfileId);
        assertThat(captor.getValue().getCvUrl()).isEqualTo("https://cv.pdf");
        verify(jobPostingRepository).incrementApplicantCount(jobPostingId);
    }

    @Test
    @DisplayName("should throw NotFoundException when job posting does not exist")
    void shouldThrowWhenJobPostingNotFound() {
        when(jobPostingRepository.findById(jobPostingId)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                applicationApi.submitApplication(jobPostingId, studentProfileId,
                        "https://cv.pdf", universityId, false))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Job posting not found");

        verify(jobApplicationRepository, never()).save(any());
    }

    @Test
    @DisplayName("should throw InvalidOperationException when job posting is a DRAFT")
    void shouldThrowWhenJobPostingIsDraft() {
        JobPosting draft = buildPosting(JobPostingStatus.DRAFT, JobType.INTERNSHIP,
                LocalDate.now().plusDays(30));
        when(jobPostingRepository.findById(jobPostingId)).thenReturn(Optional.of(draft));

        assertThatThrownBy(() ->
                applicationApi.submitApplication(jobPostingId, studentProfileId,
                        "https://cv.pdf", universityId, false))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("PUBLISHED");
    }

    @Test
    @DisplayName("should throw InvalidOperationException when job posting is CLOSED")
    void shouldThrowWhenJobPostingIsClosed() {
        JobPosting closed = buildPosting(JobPostingStatus.CLOSED, JobType.INTERNSHIP,
                LocalDate.now().plusDays(30));
        when(jobPostingRepository.findById(jobPostingId)).thenReturn(Optional.of(closed));

        assertThatThrownBy(() ->
                applicationApi.submitApplication(jobPostingId, studentProfileId,
                        "https://cv.pdf", universityId, false))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("PUBLISHED");
    }

    @Test
    @DisplayName("should throw InvalidOperationException when deadline has passed")
    void shouldThrowWhenDeadlinePassed() {
        JobPosting expired = buildPosting(JobPostingStatus.PUBLISHED, JobType.INTERNSHIP,
                LocalDate.now().minusDays(1));
        when(jobPostingRepository.findById(jobPostingId)).thenReturn(Optional.of(expired));

        assertThatThrownBy(() ->
                applicationApi.submitApplication(jobPostingId, studentProfileId,
                        "https://cv.pdf", universityId, false))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("deadline");
    }

    @Test
    @DisplayName("should throw when deadline is today (not strictly before)")
    void shouldThrowWhenDeadlineIsToday() {
        JobPosting today = buildPosting(JobPostingStatus.PUBLISHED, JobType.INTERNSHIP,
                LocalDate.now());
        when(jobPostingRepository.findById(jobPostingId)).thenReturn(Optional.of(today));

        assertThatThrownBy(() ->
                applicationApi.submitApplication(jobPostingId, studentProfileId,
                        "https://cv.pdf", universityId, false))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("deadline");
    }


    @Test
    @DisplayName("should throw InvalidOperationException when universityProfileId is null")
    void shouldThrowWhenUniversityNotSet() {
        when(jobPostingRepository.findById(jobPostingId)).thenReturn(Optional.of(publishedPosting));

        assertThatThrownBy(() ->
                applicationApi.submitApplication(jobPostingId, studentProfileId,
                        "https://cv.pdf", null, false))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("university");
    }

    @Test
    @DisplayName("should throw InvalidOperationException when company is not a partner of the university")
    void shouldThrowWhenCompanyNotPartner() {
        when(jobPostingRepository.findById(jobPostingId)).thenReturn(Optional.of(publishedPosting));
        when(universityPartnershipApi.getActivePartnerCompanyIds(universityId))
                .thenReturn(Set.of(UUID.randomUUID())); // different company

        assertThatThrownBy(() ->
                applicationApi.submitApplication(jobPostingId, studentProfileId,
                        "https://cv.pdf", universityId, false))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("partnership");
    }

    @Test
    @DisplayName("should throw when non-graduated student applies to a FULL_TIME posting")
    void shouldThrowWhenNonGraduateAppliesForFullTime() {
        JobPosting fullTime = buildPosting(JobPostingStatus.PUBLISHED, JobType.FULL_TIME,
                LocalDate.now().plusDays(30));
        when(jobPostingRepository.findById(jobPostingId)).thenReturn(Optional.of(fullTime));
        when(universityPartnershipApi.getActivePartnerCompanyIds(universityId))
                .thenReturn(Set.of(companyId));

        assertThatThrownBy(() ->
                applicationApi.submitApplication(jobPostingId, studentProfileId,
                        "https://cv.pdf", universityId, false)) // isCertVerified = false
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("Non-graduated");
    }

    @Test
    @DisplayName("should allow a FULL_TIME application when the student has a verified cert")
    void shouldAllowFullTimeWhenCertVerified() {
        JobPosting fullTime = buildPosting(JobPostingStatus.PUBLISHED, JobType.FULL_TIME,
                LocalDate.now().plusDays(30));
        when(jobPostingRepository.findById(jobPostingId)).thenReturn(Optional.of(fullTime));
        when(universityPartnershipApi.getActivePartnerCompanyIds(universityId))
                .thenReturn(Set.of(companyId));
        when(jobApplicationRepository.existsByJobPostingIdAndStudentProfileId(
                jobPostingId, studentProfileId)).thenReturn(false);
        when(jobApplicationRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        assertThatNoException().isThrownBy(() ->
                applicationApi.submitApplication(jobPostingId, studentProfileId,
                        "https://cv.pdf", universityId, true)); // isCertVerified = true
    }

    @Test
    @DisplayName("should allow INTERNSHIP applications for non-graduated students")
    void shouldAllowInternshipForNonGraduate() {
        when(jobPostingRepository.findById(jobPostingId)).thenReturn(Optional.of(publishedPosting));
        when(universityPartnershipApi.getActivePartnerCompanyIds(universityId))
                .thenReturn(Set.of(companyId));
        when(jobApplicationRepository.existsByJobPostingIdAndStudentProfileId(
                jobPostingId, studentProfileId)).thenReturn(false);
        when(jobApplicationRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        assertThatNoException().isThrownBy(() ->
                applicationApi.submitApplication(jobPostingId, studentProfileId,
                        "https://cv.pdf", universityId, false));
    }

    @Test
    @DisplayName("should throw InvalidOperationException on duplicate application")
    void shouldThrowOnDuplicateApplication() {
        when(jobPostingRepository.findById(jobPostingId)).thenReturn(Optional.of(publishedPosting));
        when(universityPartnershipApi.getActivePartnerCompanyIds(universityId))
                .thenReturn(Set.of(companyId));
        when(jobApplicationRepository.existsByJobPostingIdAndStudentProfileId(
                jobPostingId, studentProfileId)).thenReturn(true);

        assertThatThrownBy(() ->
                applicationApi.submitApplication(jobPostingId, studentProfileId,
                        "https://cv.pdf", universityId, false))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("already applied");

        verify(jobApplicationRepository, never()).save(any());
        verify(jobPostingRepository, never()).incrementApplicantCount(any());
    }

    private JobPosting buildPosting(JobPostingStatus status, JobType jobType, LocalDate deadline) {
        JobPosting p = JobPosting.builder()
                .companyId(companyId)
                .title("Engineer")
                .jobType(jobType)
                .deadline(deadline)
                .status(status)
                .build();
        ReflectionTestUtils.setField(p, "id", jobPostingId);
        return p;
    }
}