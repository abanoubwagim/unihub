package com.unihub.student.application.impl;


import com.unihub.company.api.dto.external.JobPostingPublicInfo;
import com.unihub.company.domain.enums.WorkLocationType;
import com.unihub.shared.api.dto.PageResponse;
import com.unihub.shared.api.external.CompanyApplicationApi;
import com.unihub.shared.api.external.CompanyJobPublicApi;
import com.unihub.shared.api.external.UniversityPartnershipApi;
import com.unihub.shared.domain.enums.JobType;
import com.unihub.shared.exception.InvalidOperationException;
import com.unihub.shared.exception.NotFoundException;
import com.unihub.shared.storage.FileStorageService;
import com.unihub.student.domain.model.StudentProfile;
import com.unihub.student.domain.repository.StudentProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("StudentJobUseCase Tests")
class StudentJobUseCaseTest {

    private final UUID userId = UUID.randomUUID();
    private final UUID profileId = UUID.randomUUID();
    private final UUID universityId = UUID.randomUUID();
    private final UUID companyId = UUID.randomUUID();
    private final UUID jobPostingId = UUID.randomUUID();

    @Mock
    private StudentProfileRepository studentProfileRepository;

    @Mock
    private UniversityPartnershipApi universityPartnershipApi;

    @Mock
    private CompanyJobPublicApi companyJobPublicApi;

    @Mock
    private CompanyApplicationApi companyApplicationApi;

    @Mock
    private FileStorageService fileStorageService;

    @InjectMocks
    private StudentJobUseCaseImpl jobUseCase;

    private StudentProfile profile;

    @BeforeEach
    void setUp() {
        profile = new StudentProfile();
        ReflectionTestUtils.setField(profile, "id", profileId);
        ReflectionTestUtils.setField(profile, "userId", userId);
        profile.setUniversityId(universityId);
    }

    @Test
    @DisplayName("should return available jobs from partner companies")
    void shouldReturnAvailableJobs() {
        Pageable pageable = PageRequest.of(0, 20);
        JobPostingPublicInfo job = buildJobInfo(companyId);
        Page<JobPostingPublicInfo> jobsPage = new PageImpl<>(List.of(job), pageable, 1);

        when(studentProfileRepository.findByUserId(userId)).thenReturn(Optional.of(profile));
        when(universityPartnershipApi.getActivePartnerCompanyIds(universityId)).thenReturn(Set.of(companyId));
        when(companyJobPublicApi.getPublishedJobsByCompanyIds(Set.of(companyId), pageable)).thenReturn(jobsPage);

        PageResponse<JobPostingPublicInfo> response = jobUseCase.getAvailableJobs(userId, pageable);

        assertThat(response.content()).hasSize(1);
        assertThat(response.content().get(0).companyId()).isEqualTo(companyId);
    }

    @Test
    @DisplayName("should return empty page when student has no university set")
    void shouldReturnEmptyWhenNoUniversity() {
        profile.setUniversityId(null);
        Pageable pageable = PageRequest.of(0, 20);

        when(studentProfileRepository.findByUserId(userId)).thenReturn(Optional.of(profile));

        PageResponse<JobPostingPublicInfo> response = jobUseCase.getAvailableJobs(userId, pageable);

        assertThat(response.content()).isEmpty();
        verify(universityPartnershipApi, never()).getActivePartnerCompanyIds(any());
    }

    @Test
    @DisplayName("should return empty page when university has no partner companies")
    void shouldReturnEmptyWhenNoPartnerCompanies() {
        Pageable pageable = PageRequest.of(0, 20);

        when(studentProfileRepository.findByUserId(userId)).thenReturn(Optional.of(profile));
        when(universityPartnershipApi.getActivePartnerCompanyIds(universityId)).thenReturn(Set.of());

        PageResponse<JobPostingPublicInfo> response = jobUseCase.getAvailableJobs(userId, pageable);

        assertThat(response.content()).isEmpty();
        verify(companyJobPublicApi, never()).getPublishedJobsByCompanyIds(any(), any());
    }

    @Test
    @DisplayName("should throw NotFoundException when student profile not found on getAvailableJobs")
    void shouldThrowWhenProfileNotFoundOnGetJobs() {
        when(studentProfileRepository.findByUserId(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> jobUseCase.getAvailableJobs(userId, PageRequest.of(0, 20)))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Student profile not found");
    }


    @Test
    @DisplayName("should return job detail when job is from a partner company")
    void shouldReturnJobDetailFromPartnerCompany() {
        JobPostingPublicInfo job = buildJobInfo(companyId);

        when(studentProfileRepository.findByUserId(userId)).thenReturn(Optional.of(profile));
        when(universityPartnershipApi.getActivePartnerCompanyIds(universityId)).thenReturn(Set.of(companyId));
        when(companyJobPublicApi.getPublishedJobById(jobPostingId)).thenReturn(Optional.of(job));

        JobPostingPublicInfo result = jobUseCase.getJobDetail(userId, jobPostingId);

        assertThat(result.companyId()).isEqualTo(companyId);
    }

    @Test
    @DisplayName("should throw InvalidOperationException when student has no university set on getJobDetail")
    void shouldThrowWhenNoUniversityOnGetJobDetail() {
        profile.setUniversityId(null);

        when(studentProfileRepository.findByUserId(userId)).thenReturn(Optional.of(profile));

        assertThatThrownBy(() -> jobUseCase.getJobDetail(userId, jobPostingId))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("set your university first");
    }

    @Test
    @DisplayName("should throw NotFoundException when job posting not found")
    void shouldThrowWhenJobNotFound() {
        when(studentProfileRepository.findByUserId(userId)).thenReturn(Optional.of(profile));
        when(universityPartnershipApi.getActivePartnerCompanyIds(universityId)).thenReturn(Set.of(companyId));
        when(companyJobPublicApi.getPublishedJobById(jobPostingId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> jobUseCase.getJobDetail(userId, jobPostingId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Job posting not found");
    }

    @Test
    @DisplayName("should throw InvalidOperationException when job company is not a partner")
    void shouldThrowWhenJobCompanyNotPartner() {
        UUID nonPartnerCompanyId = UUID.randomUUID();
        JobPostingPublicInfo job = buildJobInfo(nonPartnerCompanyId);

        when(studentProfileRepository.findByUserId(userId)).thenReturn(Optional.of(profile));
        when(universityPartnershipApi.getActivePartnerCompanyIds(universityId)).thenReturn(Set.of(companyId));
        when(companyJobPublicApi.getPublishedJobById(jobPostingId)).thenReturn(Optional.of(job));

        assertThatThrownBy(() -> jobUseCase.getJobDetail(userId, jobPostingId))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("not available for students of your university");
    }

    @Test
    @DisplayName("should apply to job successfully")
    void shouldApplyToJobSuccessfully() {
        JobPostingPublicInfo job = buildJobInfo(companyId);
        MockMultipartFile cv = new MockMultipartFile("cv", "cv.pdf", "application/pdf", "pdf".getBytes());

        when(studentProfileRepository.findByUserId(userId)).thenReturn(Optional.of(profile));
        when(universityPartnershipApi.getActivePartnerCompanyIds(universityId)).thenReturn(Set.of(companyId));
        when(companyJobPublicApi.getPublishedJobById(jobPostingId)).thenReturn(Optional.of(job));
        when(companyApplicationApi.hasStudentAlreadyApplied(any(), any())).thenReturn(false);
        when(fileStorageService.upload(any(), anyString())).thenReturn("https://storage/cv.pdf");

        assertThatNoException().isThrownBy(() -> jobUseCase.applyToJob(userId, jobPostingId, cv));

        verify(fileStorageService).upload(eq(cv), contains("students/cvs/" + profileId + "/" + jobPostingId));
        verify(companyApplicationApi).submitApplication(eq(jobPostingId), eq(profileId), eq("https://storage/cv.pdf"), any(), anyBoolean());
    }

    @Test
    @DisplayName("should throw InvalidOperationException when applying to a job without a CV")
    void shouldThrowWhenCvFileIsNull() {
        JobPostingPublicInfo job = buildJobInfo(companyId);

        when(studentProfileRepository.findByUserId(userId)).thenReturn(Optional.of(profile));
        when(universityPartnershipApi.getActivePartnerCompanyIds(universityId)).thenReturn(Set.of(companyId));
        when(companyJobPublicApi.getPublishedJobById(jobPostingId)).thenReturn(Optional.of(job));

        assertThatThrownBy(() -> jobUseCase.applyToJob(userId, jobPostingId, null))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("CV file is required");

        verify(fileStorageService, never()).upload(any(), any());
        verify(companyApplicationApi, never()).submitApplication(any(), any(), any(), any(), anyBoolean());
    }

    @Test
    @DisplayName("should throw InvalidOperationException when applying to a job with an empty CV")
    void shouldThrowWhenCvFileIsEmpty() {
        JobPostingPublicInfo job = buildJobInfo(companyId);
        MockMultipartFile emptyFile = new MockMultipartFile("cv", new byte[0]);

        when(studentProfileRepository.findByUserId(userId)).thenReturn(Optional.of(profile));
        when(universityPartnershipApi.getActivePartnerCompanyIds(universityId)).thenReturn(Set.of(companyId));
        when(companyJobPublicApi.getPublishedJobById(jobPostingId)).thenReturn(Optional.of(job));

        assertThatThrownBy(() -> jobUseCase.applyToJob(userId, jobPostingId, emptyFile))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("CV file is required");
    }

    @Test
    @DisplayName("should throw InvalidOperationException when student already applied to the same job")
    void shouldThrowWhenAlreadyApplied() {
        JobPostingPublicInfo job = buildJobInfo(companyId);
        MockMultipartFile cv = new MockMultipartFile("cv", "cv.pdf", "application/pdf", "pdf".getBytes());

        when(studentProfileRepository.findByUserId(userId)).thenReturn(Optional.of(profile));
        when(universityPartnershipApi.getActivePartnerCompanyIds(universityId)).thenReturn(Set.of(companyId));
        when(companyJobPublicApi.getPublishedJobById(jobPostingId)).thenReturn(Optional.of(job));
        when(companyApplicationApi.hasStudentAlreadyApplied(jobPostingId, profileId)).thenReturn(true);

        assertThatThrownBy(() -> jobUseCase.applyToJob(userId, jobPostingId, cv))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("already applied");

        verify(fileStorageService, never()).upload(any(), any());
    }

    @Test
    @DisplayName("should throw InvalidOperationException when job company is not a partner on apply")
    void shouldThrowWhenCompanyNotPartnerOnApply() {
        UUID nonPartnerCompanyId = UUID.randomUUID();
        JobPostingPublicInfo job = buildJobInfo(nonPartnerCompanyId);
        MockMultipartFile cv = new MockMultipartFile("cv", "cv.pdf", "application/pdf", "pdf".getBytes());

        when(studentProfileRepository.findByUserId(userId)).thenReturn(Optional.of(profile));
        when(universityPartnershipApi.getActivePartnerCompanyIds(universityId)).thenReturn(Set.of(companyId));
        when(companyJobPublicApi.getPublishedJobById(jobPostingId)).thenReturn(Optional.of(job));

        assertThatThrownBy(() -> jobUseCase.applyToJob(userId, jobPostingId, cv))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("active partnership");

        verify(fileStorageService, never()).upload(any(), any());
    }

    @Test
    @DisplayName("should throw NotFoundException when profile not found on apply")
    void shouldThrowWhenProfileNotFoundOnApply() {
        MockMultipartFile cv = new MockMultipartFile("cv", "cv.pdf", "application/pdf", "pdf".getBytes());
        when(studentProfileRepository.findByUserId(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> jobUseCase.applyToJob(userId, jobPostingId, cv))
                .isInstanceOf(NotFoundException.class);
    }

    private JobPostingPublicInfo buildJobInfo(UUID company) {
        return new JobPostingPublicInfo(
                jobPostingId,
                company,
                "Java Backend Developer",
                JobType.FULL_TIME,
                WorkLocationType.REMOTE,
                3000,
                5000,
                "A java backend developer role.",
                LocalDate.now().plusMonths(1),
                0,
                LocalDateTime.now()
        );
    }
}