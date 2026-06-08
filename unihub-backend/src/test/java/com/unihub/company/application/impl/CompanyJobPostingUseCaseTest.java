package com.unihub.company.application.impl;

import com.unihub.company.api.dto.req.CreateJobPostingRequest;
import com.unihub.company.api.dto.req.UpdateJobPostingRequest;
import com.unihub.company.api.dto.res.JobPostingResponse;
import com.unihub.company.api.dto.res.JobPostingSummaryResponse;
import com.unihub.company.domain.enums.JobPostingStatus;
import com.unihub.company.domain.enums.WorkLocationType;
import com.unihub.company.domain.model.CompanyProfile;
import com.unihub.company.domain.model.JobPosting;
import com.unihub.company.domain.repository.CompanyProfileRepository;
import com.unihub.company.domain.repository.JobPostingRepository;
import com.unihub.shared.api.dto.PageResponse;
import com.unihub.shared.domain.enums.JobType;
import com.unihub.shared.exception.InvalidOperationException;
import com.unihub.shared.exception.NotFoundException;
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
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CompanyJobPostingUseCase Tests")
class CompanyJobPostingUseCaseTest {

    private final UUID userId = UUID.randomUUID();
    private final UUID profileId = UUID.randomUUID();
    private final UUID postingId = UUID.randomUUID();

    @Mock
    private CompanyProfileRepository companyProfileRepository;

    @Mock
    private JobPostingRepository jobPostingRepository;

    @InjectMocks
    private CompanyJobPostingUseCaseImpl jobPostingUseCase;

    private CompanyProfile profile;

    @BeforeEach
    void setUp() {
        profile = CompanyProfile.builder()
                .id(profileId)
                .userId(userId)
                .build();
    }

    @Test
    @DisplayName("should save as DRAFT when publishNow is false")
    void shouldSaveAsDraftWhenPublishNowFalse() {
        CreateJobPostingRequest req = new CreateJobPostingRequest(
                "Engineer", JobType.FULL_TIME, WorkLocationType.REMOTE,
                3000, 5000, "Good job", LocalDate.now().plusDays(10), false);

        stubProfile();
        when(jobPostingRepository.save(any())).thenAnswer(inv -> {
            JobPosting p = inv.getArgument(0);
            ReflectionTestUtils.setField(p, "id", postingId);
            return p;
        });

        JobPostingResponse response = jobPostingUseCase.createDraft(userId, req);

        assertThat(response.status()).isEqualTo(JobPostingStatus.DRAFT);
        assertThat(response.publishedAt()).isNull();
    }

    @Test
    @DisplayName("should publish immediately when publishNow is true and posting is valid")
    void shouldPublishImmediatelyWhenPublishNowTrue() {
        CreateJobPostingRequest req = new CreateJobPostingRequest(
                "Engineer", JobType.FULL_TIME, WorkLocationType.REMOTE,
                null, null, "Good job", LocalDate.now().plusDays(10), true);

        stubProfile();
        when(jobPostingRepository.save(any())).thenAnswer(inv -> {
            JobPosting p = inv.getArgument(0);
            ReflectionTestUtils.setField(p, "id", postingId);
            return p;
        });

        JobPostingResponse response = jobPostingUseCase.createDraft(userId, req);

        assertThat(response.status()).isEqualTo(JobPostingStatus.PUBLISHED);
        assertThat(response.publishedAt()).isNotNull();
    }

    @Test
    @DisplayName("should throw InvalidOperationException when publishNow=true but title is blank")
    void shouldThrowWhenPublishNowTrueAndTitleBlank() {
        CreateJobPostingRequest req = new CreateJobPostingRequest(
                "", JobType.FULL_TIME, WorkLocationType.REMOTE,
                null, null, "Desc", LocalDate.now().plusDays(10), true);

        stubProfile();
        when(jobPostingRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        assertThatThrownBy(() -> jobPostingUseCase.createDraft(userId, req))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("Title");
    }

    @Test
    @DisplayName("should throw NotFoundException when company profile not found on createDraft")
    void createDraft_shouldThrowWhenProfileNotFound() {
        when(companyProfileRepository.findByUserId(userId)).thenReturn(Optional.empty());
        CreateJobPostingRequest req = new CreateJobPostingRequest(
                "Engineer", JobType.FULL_TIME, WorkLocationType.REMOTE,
                null, null, "Desc", LocalDate.now().plusDays(10), false);

        assertThatThrownBy(() -> jobPostingUseCase.createDraft(userId, req))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Company profile not found");

        verify(jobPostingRepository, never()).save(any());
    }

    @Test
    @DisplayName("should call save twice when publishNow=true (once as DRAFT, once as PUBLISHED)")
    void shouldSaveTwiceWhenPublishNow() {
        CreateJobPostingRequest req = new CreateJobPostingRequest(
                "Engineer", JobType.FULL_TIME, WorkLocationType.REMOTE,
                null, null, "Good job", LocalDate.now().plusDays(10), true);

        stubProfile();
        when(jobPostingRepository.save(any())).thenAnswer(inv -> {
            JobPosting p = inv.getArgument(0);
            ReflectionTestUtils.setField(p, "id", postingId);
            return p;
        });

        jobPostingUseCase.createDraft(userId, req);

        verify(jobPostingRepository, times(2)).save(any());
    }

    @Test
    @DisplayName("should update all non-null fields when posting is DRAFT")
    void shouldUpdateNonNullFields() {
        JobPosting draft = buildDraft();
        draft.setTitle("Old Title");
        UpdateJobPostingRequest req = new UpdateJobPostingRequest(
                "New Title", JobType.PART_TIME, WorkLocationType.ONSITE,
                2000, 4000, "Updated desc", LocalDate.now().plusDays(60));

        stubProfile();
        stubPosting(draft);
        stubSave();

        JobPostingResponse response = jobPostingUseCase.updateDraft(userId, postingId, req);

        assertThat(response.title()).isEqualTo("New Title");
        assertThat(response.jobType()).isEqualTo(JobType.PART_TIME);
        assertThat(response.workLocationType()).isEqualTo(WorkLocationType.ONSITE);
        assertThat(response.salaryFrom()).isEqualTo(2000);
        assertThat(response.salaryTo()).isEqualTo(4000);
        assertThat(response.description()).isEqualTo("Updated desc");
    }

    @Test
    @DisplayName("should perform partial update — null fields must NOT overwrite existing values")
    void shouldPerformPartialUpdate() {
        JobPosting draft = buildDraft();
        draft.setTitle("Keep This Title");
        UpdateJobPostingRequest req = new UpdateJobPostingRequest(
                null, null, null, null, null, "Only description updated", null);

        stubProfile();
        stubPosting(draft);
        stubSave();

        jobPostingUseCase.updateDraft(userId, postingId, req);

        assertThat(draft.getTitle()).isEqualTo("Keep This Title");
        assertThat(draft.getDescription()).isEqualTo("Only description updated");
    }

    @Test
    @DisplayName("should throw InvalidOperationException when posting is not DRAFT on update")
    void updateDraft_shouldThrowWhenPostingIsNotDraft() {
        JobPosting published = buildPublished();
        UpdateJobPostingRequest req = new UpdateJobPostingRequest(
                "New Title", null, null, null, null, null, null);

        stubProfile();
        stubPosting(published);

        assertThatThrownBy(() -> jobPostingUseCase.updateDraft(userId, postingId, req))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("Draft");

        verify(jobPostingRepository, never()).save(any());
    }

    @Test
    @DisplayName("should throw NotFoundException when posting not found on updateDraft")
    void updateDraft_shouldThrowWhenPostingNotFound() {
        stubProfile();
        when(jobPostingRepository.findByIdAndCompanyId(postingId, profileId))
                .thenReturn(Optional.empty());
        UpdateJobPostingRequest req = new UpdateJobPostingRequest(
                "Title", null, null, null, null, null, null);

        assertThatThrownBy(() -> jobPostingUseCase.updateDraft(userId, postingId, req))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("should set status to PUBLISHED and set publishedAt when valid DRAFT")
    void shouldPublishValidDraft() {
        stubProfile();
        stubPosting(buildDraft());
        stubSave();

        JobPostingResponse response = jobPostingUseCase.publish(userId, postingId);

        assertThat(response.status()).isEqualTo(JobPostingStatus.PUBLISHED);
        assertThat(response.publishedAt()).isNotNull();
    }

    @Test
    @DisplayName("should throw InvalidOperationException when posting is already PUBLISHED")
    void publish_shouldThrowWhenAlreadyPublished() {
        stubProfile();
        stubPosting(buildPublished());

        assertThatThrownBy(() -> jobPostingUseCase.publish(userId, postingId))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("Draft");

        verify(jobPostingRepository, never()).save(any());
    }

    @Test
    @DisplayName("should throw InvalidOperationException when title is missing on publish")
    void publish_shouldThrowWhenTitleMissing() {
        JobPosting noTitle = buildDraft();
        noTitle.setTitle(null);

        stubProfile();
        stubPosting(noTitle);

        assertThatThrownBy(() -> jobPostingUseCase.publish(userId, postingId))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("Title");
    }

    @Test
    @DisplayName("should throw InvalidOperationException when deadline is today (not strictly future)")
    void publish_shouldThrowWhenDeadlineIsToday() {
        JobPosting todayDeadline = buildDraft();
        todayDeadline.setDeadline(LocalDate.now());

        stubProfile();
        stubPosting(todayDeadline);

        assertThatThrownBy(() -> jobPostingUseCase.publish(userId, postingId))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("Deadline must be a future date");
    }

    @Test
    @DisplayName("should throw InvalidOperationException when deadline is in the past")
    void publish_shouldThrowWhenDeadlineIsPast() {
        JobPosting pastDeadline = buildDraft();
        pastDeadline.setDeadline(LocalDate.now().minusDays(1));

        stubProfile();
        stubPosting(pastDeadline);

        assertThatThrownBy(() -> jobPostingUseCase.publish(userId, postingId))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("Deadline");
    }

    @Test
    @DisplayName("should throw InvalidOperationException when salaryTo < salaryFrom")
    void publish_shouldThrowWhenSalaryToLessThanSalaryFrom() {
        JobPosting badSalary = buildDraft();
        badSalary.setSalaryFrom(5000);
        badSalary.setSalaryTo(3000);

        stubProfile();
        stubPosting(badSalary);

        assertThatThrownBy(() -> jobPostingUseCase.publish(userId, postingId))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("salaryTo");
    }

    @Test
    @DisplayName("should allow publish when salaryTo equals salaryFrom")
    void publish_shouldAllowWhenSalaryToEqualsSalaryFrom() {
        JobPosting equalSalary = buildDraft();
        equalSalary.setSalaryFrom(4000);
        equalSalary.setSalaryTo(4000);

        stubProfile();
        stubPosting(equalSalary);
        stubSave();

        assertThatNoException().isThrownBy(() -> jobPostingUseCase.publish(userId, postingId));
    }

    @Test
    @DisplayName("should allow publish when both salary fields are null")
    void publish_shouldAllowWhenBothSalaryFieldsNull() {
        JobPosting noSalary = buildDraft();
        noSalary.setSalaryFrom(null);
        noSalary.setSalaryTo(null);

        stubProfile();
        stubPosting(noSalary);
        stubSave();

        assertThatNoException().isThrownBy(() -> jobPostingUseCase.publish(userId, postingId));
    }

    @Test
    @DisplayName("should throw InvalidOperationException when jobType is null")
    void publish_shouldThrowWhenJobTypeNull() {
        JobPosting noJobType = buildDraft();
        noJobType.setJobType(null);

        stubProfile();
        stubPosting(noJobType);

        assertThatThrownBy(() -> jobPostingUseCase.publish(userId, postingId))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("Job type");
    }

    @Test
    @DisplayName("should throw InvalidOperationException when workLocationType is null")
    void publish_shouldThrowWhenWorkLocationTypeNull() {
        JobPosting noLocation = buildDraft();
        noLocation.setWorkLocationType(null);

        stubProfile();
        stubPosting(noLocation);

        assertThatThrownBy(() -> jobPostingUseCase.publish(userId, postingId))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("Work location");
    }

    @Test
    @DisplayName("should throw InvalidOperationException when description is blank")
    void publish_shouldThrowWhenDescriptionBlank() {
        JobPosting blankDesc = buildDraft();
        blankDesc.setDescription("   ");

        stubProfile();
        stubPosting(blankDesc);

        assertThatThrownBy(() -> jobPostingUseCase.publish(userId, postingId))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("Description");
    }

    @Test
    @DisplayName("should set status to CLOSED when posting is PUBLISHED")
    void shouldClosePublishedPosting() {
        stubProfile();
        stubPosting(buildPublished());
        stubSave();

        JobPostingResponse response = jobPostingUseCase.close(userId, postingId);

        assertThat(response.status()).isEqualTo(JobPostingStatus.CLOSED);
    }

    @Test
    @DisplayName("should throw InvalidOperationException when posting is DRAFT on close")
    void close_shouldThrowWhenPostingIsDraft() {
        stubProfile();
        stubPosting(buildDraft());

        assertThatThrownBy(() -> jobPostingUseCase.close(userId, postingId))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("PUBLISHED");

        verify(jobPostingRepository, never()).save(any());
    }

    @Test
    @DisplayName("should throw NotFoundException when posting not found on close")
    void close_shouldThrowWhenPostingNotFound() {
        stubProfile();
        when(jobPostingRepository.findByIdAndCompanyId(postingId, profileId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> jobPostingUseCase.close(userId, postingId))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("should delete a DRAFT posting successfully")
    void shouldDeleteDraftPosting() {
        JobPosting draft = buildDraft();
        stubProfile();
        stubPosting(draft);

        assertThatNoException().isThrownBy(() -> jobPostingUseCase.delete(userId, postingId));

        verify(jobPostingRepository).delete(draft);
    }

    @Test
    @DisplayName("should throw InvalidOperationException when trying to delete a PUBLISHED posting")
    void delete_shouldThrowWhenDeletingPublishedPosting() {
        stubProfile();
        stubPosting(buildPublished());

        assertThatThrownBy(() -> jobPostingUseCase.delete(userId, postingId))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("DRAFT");

        verify(jobPostingRepository, never()).delete(any());
    }

    @Test
    @DisplayName("should throw NotFoundException when posting not found on delete")
    void delete_shouldThrowWhenPostingNotFound() {
        stubProfile();
        when(jobPostingRepository.findByIdAndCompanyId(postingId, profileId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> jobPostingUseCase.delete(userId, postingId))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("should return all postings when no status filter is provided")
    void shouldReturnAllPostingsWithoutFilter() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<JobPosting> page = new PageImpl<>(List.of(buildDraft()), pageable, 1);

        stubProfile();
        when(jobPostingRepository.findAllByCompanyId(profileId, pageable)).thenReturn(page);

        PageResponse<JobPostingSummaryResponse> result = jobPostingUseCase.getAll(userId, null, pageable);

        assertThat(result.content()).hasSize(1);
        verify(jobPostingRepository, never()).findAllByCompanyIdAndStatus(any(), any(), any());
    }

    @Test
    @DisplayName("should filter by status when status filter is provided")
    void shouldFilterByStatusWhenProvided() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<JobPosting> page = new PageImpl<>(List.of(buildPublished()), pageable, 1);

        stubProfile();
        when(jobPostingRepository.findAllByCompanyIdAndStatus(profileId, JobPostingStatus.PUBLISHED, pageable))
                .thenReturn(page);

        PageResponse<JobPostingSummaryResponse> result =
                jobPostingUseCase.getAll(userId, JobPostingStatus.PUBLISHED, pageable);

        assertThat(result.content()).hasSize(1);
        verify(jobPostingRepository, never()).findAllByCompanyId(any(), any());
    }

    @Test
    @DisplayName("should return single posting by id")
    void shouldReturnSinglePosting() {
        stubProfile();
        stubPosting(buildDraft());

        JobPostingResponse response = jobPostingUseCase.getById(userId, postingId);

        assertThat(response.id()).isEqualTo(postingId);
        assertThat(response.companyId()).isEqualTo(profileId);
    }

    @Test
    @DisplayName("should throw NotFoundException when posting not found by id")
    void getById_shouldThrowWhenPostingNotFound() {
        stubProfile();
        when(jobPostingRepository.findByIdAndCompanyId(postingId, profileId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> jobPostingUseCase.getById(userId, postingId))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("should throw NotFoundException when profile not found on getAll")
    void getAll_shouldThrowWhenProfileNotFound() {
        when(companyProfileRepository.findByUserId(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> jobPostingUseCase.getAll(userId, null, PageRequest.of(0, 20)))
                .isInstanceOf(NotFoundException.class);
    }

    private JobPosting buildDraft() {
        return JobPosting.builder()
                .id(postingId)
                .companyId(profileId)
                .title("Backend Engineer")
                .jobType(JobType.FULL_TIME)
                .workLocationType(WorkLocationType.REMOTE)
                .description("Great role")
                .deadline(LocalDate.now().plusDays(30))
                .status(JobPostingStatus.DRAFT)
                .build();
    }

    private JobPosting buildPublished() {
        JobPosting p = buildDraft();
        p.setStatus(JobPostingStatus.PUBLISHED);
        p.setPublishedAt(LocalDateTime.now());
        return p;
    }

    private void stubProfile() {
        when(companyProfileRepository.findByUserId(userId)).thenReturn(Optional.of(profile));
    }

    private void stubPosting(JobPosting posting) {
        when(jobPostingRepository.findByIdAndCompanyId(postingId, profileId))
                .thenReturn(Optional.of(posting));
    }

    private void stubSave() {
        when(jobPostingRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }
}