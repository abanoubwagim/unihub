package com.unihub.student.application.impl;

import com.unihub.shared.api.dto.PageResponse;
import com.unihub.shared.domain.enums.JobType;
import com.unihub.shared.exception.NotFoundException;
import com.unihub.student.api.dto.req.ExperienceRequest;
import com.unihub.student.api.dto.res.ExperienceResponse;
import com.unihub.student.domain.model.Skill;
import com.unihub.student.domain.model.StudentExperience;
import com.unihub.student.domain.model.StudentProfile;
import com.unihub.student.domain.repository.SkillRepository;
import com.unihub.student.domain.repository.StudentExperienceRepository;
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
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("StudentExperienceUseCase Tests")
class StudentExperienceUseCaseTest {

    private final UUID userId = UUID.randomUUID();
    private final UUID experienceId = UUID.randomUUID();

    @Mock
    private StudentProfileRepository profileRepository;

    @Mock
    private StudentExperienceRepository experienceRepository;

    @Mock
    private SkillRepository skillRepository;

    @InjectMocks
    private StudentExperienceUseCaseImpl experienceUseCase;

    private StudentProfile profile;

    @BeforeEach
    void setUp() {
        profile = new StudentProfile();
        ReflectionTestUtils.setField(profile, "id", UUID.randomUUID());
        ReflectionTestUtils.setField(profile, "userId", userId);
    }

    @Test
    @DisplayName("should add a non-current experience with end date successfully")
    void shouldAddNonCurrentExperienceSuccessfully() {
        ExperienceRequest request = buildRequest(false, LocalDate.of(2023, 1, 1), LocalDate.of(2024, 1, 1), null);

        when(profileRepository.findByUserId(userId)).thenReturn(Optional.of(profile));
        when(experienceRepository.save(any())).thenAnswer(inv -> {
            StudentExperience e = inv.getArgument(0);
            ReflectionTestUtils.setField(e, "id", experienceId);
            return e;
        });

        ExperienceResponse response = experienceUseCase.add(userId, request);

        assertThat(response).isNotNull();
        assertThat(response.jobTitle()).isEqualTo("Backend Developer");
        assertThat(response.company()).isEqualTo("Acme Corp");
        assertThat(response.current()).isFalse();
        assertThat(response.endDate()).isEqualTo(LocalDate.of(2024, 1, 1));
    }

    @Test
    @DisplayName("should add a current experience without end date")
    void shouldAddCurrentExperienceWithoutEndDate() {
        ExperienceRequest request = buildRequest(true, LocalDate.of(2024, 1, 1), null, null);

        when(profileRepository.findByUserId(userId)).thenReturn(Optional.of(profile));
        when(experienceRepository.save(any())).thenAnswer(inv -> {
            StudentExperience e = inv.getArgument(0);
            ReflectionTestUtils.setField(e, "id", experienceId);
            return e;
        });

        ExperienceResponse response = experienceUseCase.add(userId, request);

        assertThat(response.current()).isTrue();
        assertThat(response.endDate()).isNull();
    }

    @Test
    @DisplayName("should throw NotFoundException when student profile not found on add")
    void shouldThrowWhenProfileNotFoundOnAdd() {
        when(profileRepository.findByUserId(userId)).thenReturn(Optional.empty());

        ExperienceRequest request = buildRequest(false, LocalDate.of(2023, 1, 1), LocalDate.of(2024, 1, 1), null);

        assertThatThrownBy(() -> experienceUseCase.add(userId, request))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Student profile not found");

        verify(experienceRepository, never()).save(any());
    }

    @Test
    @DisplayName("should throw IllegalArgumentException when current=true but endDate is set")
    void shouldThrowWhenCurrentTrueButEndDateSet() {
        ExperienceRequest request = buildRequest(true, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 6, 1), null);
        when(profileRepository.findByUserId(userId)).thenReturn(Optional.of(profile));

        assertThatThrownBy(() -> experienceUseCase.add(userId, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("end date");

        verify(experienceRepository, never()).save(any());
    }

    @Test
    @DisplayName("should throw IllegalArgumentException when current=false but endDate is null")
    void shouldThrowWhenCurrentFalseButEndDateNull() {
        ExperienceRequest request = buildRequest(false, LocalDate.of(2024, 1, 1), null, null);
        when(profileRepository.findByUserId(userId)).thenReturn(Optional.of(profile));

        assertThatThrownBy(() -> experienceUseCase.add(userId, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("End date is required");

        verify(experienceRepository, never()).save(any());
    }

    @Test
    @DisplayName("should throw IllegalArgumentException when startDate is after endDate")
    void shouldThrowWhenStartDateAfterEndDate() {
        ExperienceRequest request = buildRequest(false, LocalDate.of(2025, 1, 1), LocalDate.of(2024, 1, 1), null);
        when(profileRepository.findByUserId(userId)).thenReturn(Optional.of(profile));

        assertThatThrownBy(() -> experienceUseCase.add(userId, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Start date cannot be after end date");

        verify(experienceRepository, never()).save(any());
    }

    @Test
    @DisplayName("should assign skills correctly when valid skillIds provided")
    void shouldAssignSkillsOnAdd() {
        UUID skillId1 = UUID.randomUUID();
        UUID skillId2 = UUID.randomUUID();
        Skill skill1 = Skill.builder().id(skillId1).name("Java").build();
        Skill skill2 = Skill.builder().id(skillId2).name("Spring Boot").build();

        ExperienceRequest request = buildRequest(false, LocalDate.of(2023, 1, 1), LocalDate.of(2024, 1, 1),
                Set.of(skillId1, skillId2));

        when(profileRepository.findByUserId(userId)).thenReturn(Optional.of(profile));
        when(skillRepository.findAllByIdIn(Set.of(skillId1, skillId2))).thenReturn(List.of(skill1, skill2));
        when(experienceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ExperienceResponse response = experienceUseCase.add(userId, request);

        assertThat(response.skills()).containsExactlyInAnyOrder("Java", "Spring Boot");
    }

    @Test
    @DisplayName("should throw NotFoundException when one or more skillIds not found")
    void shouldThrowWhenSkillNotFound() {
        UUID skillId1 = UUID.randomUUID();
        UUID skillId2 = UUID.randomUUID();
        ExperienceRequest request = buildRequest(false, LocalDate.of(2023, 1, 1), LocalDate.of(2024, 1, 1),
                Set.of(skillId1, skillId2));

        when(profileRepository.findByUserId(userId)).thenReturn(Optional.of(profile));
        when(skillRepository.findAllByIdIn(any())).thenReturn(List.of()); // returns nothing

        assertThatThrownBy(() -> experienceUseCase.add(userId, request))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("skills not found");

        verify(experienceRepository, never()).save(any());
    }

    @Test
    @DisplayName("should update experience fields successfully")
    void shouldUpdateExperienceFields() {
        StudentExperience existing = buildExperience(false, LocalDate.of(2023, 1, 1), LocalDate.of(2023, 12, 31));
        ExperienceRequest updateReq = new ExperienceRequest(
                "Senior Dev", "New Corp", JobType.FULL_TIME,
                LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31),
                false, "Cairo", "Updated desc", null);

        when(experienceRepository.findByIdAndStudent_UserId(experienceId, userId)).thenReturn(Optional.of(existing));
        when(experienceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ExperienceResponse response = experienceUseCase.update(userId, experienceId, updateReq);

        assertThat(response.jobTitle()).isEqualTo("Senior Dev");
        assertThat(response.company()).isEqualTo("New Corp");
    }

    @Test
    @DisplayName("should throw NotFoundException when experience not found on update")
    void shouldThrowWhenExperienceNotFoundOnUpdate() {
        when(experienceRepository.findByIdAndStudent_UserId(experienceId, userId)).thenReturn(Optional.empty());

        ExperienceRequest request = buildRequest(false, LocalDate.of(2023, 1, 1), LocalDate.of(2024, 1, 1), null);

        assertThatThrownBy(() -> experienceUseCase.update(userId, experienceId, request))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Experience not found");

        verify(experienceRepository, never()).save(any());
    }

    @Test
    @DisplayName("should enforce date validation on update as well")
    void shouldEnforceDateValidationOnUpdate() {
        StudentExperience existing = buildExperience(false, LocalDate.of(2023, 1, 1), LocalDate.of(2023, 12, 31));
        ExperienceRequest badRequest = buildRequest(false, LocalDate.of(2025, 1, 1), null, null); // no end date

        when(experienceRepository.findByIdAndStudent_UserId(experienceId, userId)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> experienceUseCase.update(userId, experienceId, badRequest))
                .isInstanceOf(IllegalArgumentException.class);

        verify(experienceRepository, never()).save(any());
    }

    @Test
    @DisplayName("should delete experience successfully")
    void shouldDeleteExperienceSuccessfully() {
        StudentExperience existing = buildExperience(false, LocalDate.of(2023, 1, 1), LocalDate.of(2023, 12, 31));
        when(experienceRepository.findByIdAndStudent_UserId(experienceId, userId)).thenReturn(Optional.of(existing));

        assertThatNoException().isThrownBy(() -> experienceUseCase.delete(userId, experienceId));

        verify(experienceRepository).delete(existing);
    }

    @Test
    @DisplayName("should throw NotFoundException when experience not found on delete")
    void shouldThrowWhenExperienceNotFoundOnDelete() {
        when(experienceRepository.findByIdAndStudent_UserId(experienceId, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> experienceUseCase.delete(userId, experienceId))
                .isInstanceOf(NotFoundException.class);

        verify(experienceRepository, never()).delete(any());
    }

    @Test
    @DisplayName("should return paginated experiences for the user")
    void shouldReturnPaginatedExperiences() {
        Pageable pageable = PageRequest.of(0, 10);
        StudentExperience exp = buildExperience(false, LocalDate.of(2023, 1, 1), LocalDate.of(2023, 12, 31));
        Page<StudentExperience> page = new PageImpl<>(List.of(exp), pageable, 1);

        when(experienceRepository.findAllByStudent_UserId(userId, pageable)).thenReturn(page);

        PageResponse<ExperienceResponse> response = experienceUseCase.getAll(userId, pageable);

        assertThat(response.content()).hasSize(1);
        assertThat(response.content().get(0).jobTitle()).isEqualTo("Backend Developer");
    }

    @Test
    @DisplayName("should return single experience by id")
    void shouldReturnSingleExperience() {
        StudentExperience exp = buildExperience(false, LocalDate.of(2023, 1, 1), LocalDate.of(2023, 12, 31));
        when(experienceRepository.findByIdAndStudent_UserId(experienceId, userId)).thenReturn(Optional.of(exp));

        ExperienceResponse response = experienceUseCase.getOne(userId, experienceId);

        assertThat(response.jobTitle()).isEqualTo("Backend Developer");
    }

    @Test
    @DisplayName("should throw NotFoundException when experience not found by id")
    void shouldThrowWhenExperienceNotFoundById() {
        when(experienceRepository.findByIdAndStudent_UserId(experienceId, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> experienceUseCase.getOne(userId, experienceId))
                .isInstanceOf(NotFoundException.class);
    }

    private ExperienceRequest buildRequest(boolean current, LocalDate start, LocalDate end, Set<UUID> skillIds) {
        return new ExperienceRequest(
                "Backend Developer", "Acme Corp", JobType.FULL_TIME,
                start, end, current, "Cairo", "Some description", skillIds);
    }

    private StudentExperience buildExperience(boolean current, LocalDate start, LocalDate end) {
        StudentExperience exp = new StudentExperience();
        ReflectionTestUtils.setField(exp, "id", experienceId);
        exp.setStudent(profile);
        exp.setJobTitle("Backend Developer");
        exp.setCompany("Acme Corp");
        exp.setJobType(JobType.FULL_TIME);
        exp.setStartDate(start);
        exp.setEndDate(end);
        exp.setCurrent(current);
        exp.setSkills(new HashSet<>());
        return exp;
    }
}