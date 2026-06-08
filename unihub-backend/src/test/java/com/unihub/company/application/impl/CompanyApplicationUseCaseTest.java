package com.unihub.company.application.impl;

import com.unihub.company.api.dto.req.ReviewApplicationRequest;
import com.unihub.company.api.dto.res.ApplicationSummaryResponse;
import com.unihub.company.domain.enums.ApplicationStatus;
import com.unihub.company.domain.event.StudentHiredEvent;
import com.unihub.company.domain.model.CompanyProfile;
import com.unihub.company.domain.model.JobApplication;
import com.unihub.company.domain.model.JobPosting;
import com.unihub.company.domain.repository.CompanyProfileRepository;
import com.unihub.company.domain.repository.JobApplicationRepository;
import com.unihub.company.domain.repository.JobPostingRepository;
import com.unihub.shared.api.dto.PageResponse;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CompanyApplicationUseCase Tests")
class CompanyApplicationUseCaseTest {

    private final UUID userId = UUID.randomUUID();
    private final UUID companyId = UUID.randomUUID();
    private final UUID jobPostingId = UUID.randomUUID();
    private final UUID applicationId = UUID.randomUUID();
    private final UUID studentId = UUID.randomUUID();

    @Mock
    private CompanyProfileRepository companyProfileRepository;

    @Mock
    private JobApplicationRepository jobApplicationRepository;

    @Mock
    private JobPostingRepository jobPostingRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private CompanyApplicationUseCaseImpl applicationUseCase;

    private CompanyProfile profile;

    @BeforeEach
    void setUp() {
        profile = CompanyProfile.builder().userId(userId).name("Acme Corp").build();
        ReflectionTestUtils.setField(profile, "id", companyId);

        JobPosting jobPosting = mock(JobPosting.class);
        lenient().when(jobPostingRepository.findByIdAndCompanyId(any(), any()))
                .thenReturn(Optional.of(jobPosting));
    }

    @Test
    @DisplayName("should return paginated applications for a job posting")
    void shouldReturnPaginatedApplications() {
        Pageable pageable = PageRequest.of(0, 20);
        JobApplication app = buildApplication(ApplicationStatus.PENDING);
        Page<JobApplication> page = new PageImpl<>(List.of(app), pageable, 1);

        when(companyProfileRepository.findByUserId(userId)).thenReturn(Optional.of(profile));
        when(jobApplicationRepository.findAllByJobPostingId(jobPostingId, pageable)).thenReturn(page);

        PageResponse<ApplicationSummaryResponse> response =
                applicationUseCase.getApplications(userId, jobPostingId, pageable);

        assertThat(response.content()).hasSize(1);
        assertThat(response.totalElements()).isEqualTo(1);
        assertThat(response.content().get(0).status()).isEqualTo(ApplicationStatus.PENDING);
    }

    @Test
    @DisplayName("should return empty page when no applications exist")
    void shouldReturnEmptyPageWhenNoApplications() {
        Pageable pageable = PageRequest.of(0, 20);
        when(companyProfileRepository.findByUserId(userId)).thenReturn(Optional.of(profile));
        when(jobApplicationRepository.findAllByJobPostingId(jobPostingId, pageable))
                .thenReturn(Page.empty(pageable));

        PageResponse<ApplicationSummaryResponse> response =
                applicationUseCase.getApplications(userId, jobPostingId, pageable);

        assertThat(response.content()).isEmpty();
    }

    @Test
    @DisplayName("should throw NotFoundException when profile not found on getApplications")
    void shouldThrowWhenProfileNotFoundOnGetAll() {
        when(companyProfileRepository.findByUserId(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> applicationUseCase.getApplications(
                userId, jobPostingId, PageRequest.of(0, 20)))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Company profile not found");
    }

    @Test
    @DisplayName("should return single application by id")
    void shouldReturnSingleApplicationById() {
        JobApplication app = buildApplication(ApplicationStatus.PENDING);
        when(companyProfileRepository.findByUserId(userId)).thenReturn(Optional.of(profile));
        when(jobApplicationRepository.findByIdAndJobPosting_CompanyId(applicationId, companyId))
                .thenReturn(Optional.of(app));

        ApplicationSummaryResponse response =
                applicationUseCase.getApplication(userId, jobPostingId, applicationId);

        assertThat(response.id()).isEqualTo(applicationId);
        assertThat(response.studentProfileId()).isEqualTo(studentId);
        assertThat(response.status()).isEqualTo(ApplicationStatus.PENDING);
    }

    @Test
    @DisplayName("should throw NotFoundException when application not found by id")
    void shouldThrowWhenApplicationNotFoundById() {
        when(companyProfileRepository.findByUserId(userId)).thenReturn(Optional.of(profile));
        when(jobApplicationRepository.findByIdAndJobPosting_CompanyId(applicationId, companyId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                applicationUseCase.getApplication(userId, jobPostingId, applicationId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Application not found");
    }

    @Test
    @DisplayName("should accept a PENDING application and publish StudentHiredEvent")
    void shouldAcceptApplicationAndPublishEvent() {
        JobApplication app = buildApplication(ApplicationStatus.PENDING);
        when(companyProfileRepository.findByUserId(userId)).thenReturn(Optional.of(profile));
        when(jobApplicationRepository.findByIdAndJobPosting_CompanyId(applicationId, companyId))
                .thenReturn(Optional.of(app));
        when(jobApplicationRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        ReviewApplicationRequest req = new ReviewApplicationRequest(true, null);
        ApplicationSummaryResponse response =
                applicationUseCase.review(userId, jobPostingId, applicationId, req);

        assertThat(response.status()).isEqualTo(ApplicationStatus.ACCEPTED);
        assertThat(response.reviewedAt()).isNotNull();

        ArgumentCaptor<StudentHiredEvent> eventCaptor =
                ArgumentCaptor.forClass(StudentHiredEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        assertThat(eventCaptor.getValue().companyProfileId()).isEqualTo(companyId);
        assertThat(eventCaptor.getValue().studentProfileId()).isEqualTo(studentId);
        assertThat(eventCaptor.getValue().jobPostingId()).isEqualTo(jobPostingId);
    }

    @Test
    @DisplayName("should reject a PENDING application with a reason")
    void shouldRejectApplicationWithReason() {
        JobApplication app = buildApplication(ApplicationStatus.PENDING);
        when(companyProfileRepository.findByUserId(userId)).thenReturn(Optional.of(profile));
        when(jobApplicationRepository.findByIdAndJobPosting_CompanyId(applicationId, companyId))
                .thenReturn(Optional.of(app));
        when(jobApplicationRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        ReviewApplicationRequest req = new ReviewApplicationRequest(false, "Not qualified");
        ApplicationSummaryResponse response =
                applicationUseCase.review(userId, jobPostingId, applicationId, req);

        assertThat(response.status()).isEqualTo(ApplicationStatus.REJECTED);
        assertThat(response.rejectionReason()).isEqualTo("Not qualified");
        assertThat(response.reviewedAt()).isNotNull();
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("should throw InvalidOperationException when reviewing an already-accepted application")
    void shouldThrowWhenReviewingAlreadyAccepted() {
        JobApplication app = buildApplication(ApplicationStatus.ACCEPTED);
        when(companyProfileRepository.findByUserId(userId)).thenReturn(Optional.of(profile));
        when(jobApplicationRepository.findByIdAndJobPosting_CompanyId(applicationId, companyId))
                .thenReturn(Optional.of(app));

        assertThatThrownBy(() ->
                applicationUseCase.review(userId, jobPostingId, applicationId,
                        new ReviewApplicationRequest(true, null)))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("PENDING");

        verify(jobApplicationRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("should throw InvalidOperationException when reviewing an already-rejected application")
    void shouldThrowWhenReviewingAlreadyRejected() {
        JobApplication app = buildApplication(ApplicationStatus.REJECTED);
        when(companyProfileRepository.findByUserId(userId)).thenReturn(Optional.of(profile));
        when(jobApplicationRepository.findByIdAndJobPosting_CompanyId(applicationId, companyId))
                .thenReturn(Optional.of(app));

        assertThatThrownBy(() ->
                applicationUseCase.review(userId, jobPostingId, applicationId,
                        new ReviewApplicationRequest(false, "Reason")))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("PENDING");
    }

    @Test
    @DisplayName("should throw NotFoundException when application not found during review")
    void shouldThrowWhenApplicationNotFoundOnReview() {
        when(companyProfileRepository.findByUserId(userId)).thenReturn(Optional.of(profile));
        when(jobApplicationRepository.findByIdAndJobPosting_CompanyId(applicationId, companyId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                applicationUseCase.review(userId, jobPostingId, applicationId,
                        new ReviewApplicationRequest(true, null)))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("should throw NotFoundException when company profile not found during review")
    void shouldThrowWhenProfileNotFoundOnReview() {
        when(companyProfileRepository.findByUserId(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                applicationUseCase.review(userId, jobPostingId, applicationId,
                        new ReviewApplicationRequest(true, null)))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Company profile not found");
    }

    private JobApplication buildApplication(ApplicationStatus status) {
        JobApplication app = JobApplication.builder()
                .jobPostingId(jobPostingId)
                .studentProfileId(studentId)
                .cvUrl("https://storage/cv.pdf")
                .status(status)
                .build();
        ReflectionTestUtils.setField(app, "id", applicationId);
        ReflectionTestUtils.setField(app, "submittedAt", LocalDateTime.now().minusHours(1));
        return app;
    }
}