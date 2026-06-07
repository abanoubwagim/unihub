package com.unihub.student.application.impl;

import com.unihub.shared.api.dto.PageResponse;
import com.unihub.shared.exception.NotFoundException;
import com.unihub.student.api.dto.req.ProjectRequest;
import com.unihub.student.api.dto.res.ProjectResponse;
import com.unihub.student.domain.model.Skill;
import com.unihub.student.domain.model.StudentProfile;
import com.unihub.student.domain.model.StudentProject;
import com.unihub.student.domain.repository.SkillRepository;
import com.unihub.student.domain.repository.StudentProfileRepository;
import com.unihub.student.domain.repository.StudentProjectRepository;
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
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("StudentProjectUseCase Tests")
class StudentProjectUseCaseTest {

    private final UUID userId = UUID.randomUUID();
    private final UUID projectId = UUID.randomUUID();

    @Mock
    private StudentProfileRepository profileRepository;

    @Mock
    private StudentProjectRepository projectRepository;

    @Mock
    private SkillRepository skillRepository;

    @InjectMocks
    private StudentProjectUseCaseImpl projectUseCase;

    private StudentProfile profile;

    @BeforeEach
    void setUp() {
        profile = new StudentProfile();
        ReflectionTestUtils.setField(profile, "id", UUID.randomUUID());
        ReflectionTestUtils.setField(profile, "userId", userId);
    }

    @Test
    @DisplayName("should add a non-current project with end date successfully")
    void shouldAddNonCurrentProjectSuccessfully() {
        ProjectRequest request = buildRequest(false, LocalDate.of(2023, 1, 1), LocalDate.of(2024, 1, 1), null);

        when(profileRepository.findByUserId(userId)).thenReturn(Optional.of(profile));
        when(projectRepository.save(any())).thenAnswer(inv -> {
            StudentProject p = inv.getArgument(0);
            ReflectionTestUtils.setField(p, "id", projectId);
            return p;
        });

        ProjectResponse response = projectUseCase.add(userId, request);

        assertThat(response).isNotNull();
        assertThat(response.title()).isEqualTo("UniHub Platform");
        assertThat(response.current()).isFalse();
        assertThat(response.endDate()).isEqualTo(LocalDate.of(2024, 1, 1));
    }

    @Test
    @DisplayName("should add a current project without end date")
    void shouldAddCurrentProjectWithoutEndDate() {
        ProjectRequest request = buildRequest(true, LocalDate.of(2024, 1, 1), null, null);

        when(profileRepository.findByUserId(userId)).thenReturn(Optional.of(profile));
        when(projectRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ProjectResponse response = projectUseCase.add(userId, request);

        assertThat(response.current()).isTrue();
        assertThat(response.endDate()).isNull();
    }

    @Test
    @DisplayName("should throw NotFoundException when student profile not found")
    void shouldThrowWhenProfileNotFoundOnAdd() {
        when(profileRepository.findByUserId(userId)).thenReturn(Optional.empty());
        ProjectRequest request = buildRequest(false, LocalDate.of(2023, 1, 1), LocalDate.of(2024, 1, 1), null);

        assertThatThrownBy(() -> projectUseCase.add(userId, request))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Student profile not found");

        verify(projectRepository, never()).save(any());
    }

    @Test
    @DisplayName("should throw when current=true but endDate is set")
    void shouldThrowWhenCurrentTrueButEndDateSet() {
        ProjectRequest request = buildRequest(true, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 6, 1), null);
        when(profileRepository.findByUserId(userId)).thenReturn(Optional.of(profile));

        assertThatThrownBy(() -> projectUseCase.add(userId, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("end date");

        verify(projectRepository, never()).save(any());
    }

    @Test
    @DisplayName("should throw when current=false but endDate is null")
    void shouldThrowWhenCurrentFalseButEndDateNull() {
        ProjectRequest request = buildRequest(false, LocalDate.of(2024, 1, 1), null, null);
        when(profileRepository.findByUserId(userId)).thenReturn(Optional.of(profile));

        assertThatThrownBy(() -> projectUseCase.add(userId, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("End date is required");

        verify(projectRepository, never()).save(any());
    }

    @Test
    @DisplayName("should throw when startDate is after endDate")
    void shouldThrowWhenStartAfterEnd() {
        ProjectRequest request = buildRequest(false, LocalDate.of(2025, 1, 1), LocalDate.of(2024, 1, 1), null);
        when(profileRepository.findByUserId(userId)).thenReturn(Optional.of(profile));

        assertThatThrownBy(() -> projectUseCase.add(userId, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Start date cannot be after end date");

        verify(projectRepository, never()).save(any());
    }

    @Test
    @DisplayName("should assign skills when valid skillIds are provided")
    void shouldAssignSkillsToProject() {
        UUID skillId = UUID.randomUUID();
        Skill skill = Skill.builder().id(skillId).name("Angular").build();

        ProjectRequest request = buildRequest(false, LocalDate.of(2023, 1, 1), LocalDate.of(2024, 1, 1), Set.of(skillId));

        when(profileRepository.findByUserId(userId)).thenReturn(Optional.of(profile));
        when(skillRepository.findAllByIdIn(Set.of(skillId))).thenReturn(List.of(skill));
        when(projectRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ProjectResponse response = projectUseCase.add(userId, request);

        assertThat(response.skills()).containsExactly("Angular");
    }

    @Test
    @DisplayName("should throw NotFoundException when any skillId not found")
    void shouldThrowWhenSkillNotFound() {
        UUID skillId = UUID.randomUUID();
        ProjectRequest request = buildRequest(false, LocalDate.of(2023, 1, 1), LocalDate.of(2024, 1, 1), Set.of(skillId));

        when(profileRepository.findByUserId(userId)).thenReturn(Optional.of(profile));
        when(skillRepository.findAllByIdIn(any())).thenReturn(List.of()); // skill not found

        assertThatThrownBy(() -> projectUseCase.add(userId, request))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("skills not found");

        verify(projectRepository, never()).save(any());
    }

    @Test
    @DisplayName("should save project linked to the correct student profile")
    void shouldSaveProjectLinkedToProfile() {
        ProjectRequest request = buildRequest(false, LocalDate.of(2023, 1, 1), LocalDate.of(2024, 1, 1), null);

        when(profileRepository.findByUserId(userId)).thenReturn(Optional.of(profile));
        when(projectRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        projectUseCase.add(userId, request);

        verify(projectRepository).save(argThat(p -> p.getStudent() == profile));
    }

    @Test
    @DisplayName("should update project fields successfully")
    void shouldUpdateProjectSuccessfully() {
        StudentProject existing = buildProject(false, LocalDate.of(2023, 1, 1), LocalDate.of(2023, 12, 31));
        ProjectRequest updateReq = new ProjectRequest(
                "Updated Title", "New Desc",
                LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31),
                false, "https://github.com/new", null);

        when(projectRepository.findByIdAndStudent_UserId(projectId, userId)).thenReturn(Optional.of(existing));
        when(projectRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ProjectResponse response = projectUseCase.update(userId, projectId, updateReq);

        assertThat(response.title()).isEqualTo("Updated Title");
        assertThat(response.description()).isEqualTo("New Desc");
        assertThat(response.projectLink()).isEqualTo("https://github.com/new");
    }

    @Test
    @DisplayName("should throw NotFoundException when project not found on update")
    void shouldThrowWhenProjectNotFoundOnUpdate() {
        when(projectRepository.findByIdAndStudent_UserId(projectId, userId)).thenReturn(Optional.empty());
        ProjectRequest request = buildRequest(false, LocalDate.of(2023, 1, 1), LocalDate.of(2024, 1, 1), null);

        assertThatThrownBy(() -> projectUseCase.update(userId, projectId, request))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Project not found");

        verify(projectRepository, never()).save(any());
    }

    @Test
    @DisplayName("should enforce date validation on update")
    void shouldEnforceDateValidationOnUpdate() {
        StudentProject existing = buildProject(false, LocalDate.of(2023, 1, 1), LocalDate.of(2023, 12, 31));
        ProjectRequest badRequest = buildRequest(true, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 6, 1), null);

        when(projectRepository.findByIdAndStudent_UserId(projectId, userId)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> projectUseCase.update(userId, projectId, badRequest))
                .isInstanceOf(IllegalArgumentException.class);

        verify(projectRepository, never()).save(any());
    }

    @Test
    @DisplayName("should delete project successfully")
    void shouldDeleteProjectSuccessfully() {
        StudentProject existing = buildProject(false, LocalDate.of(2023, 1, 1), LocalDate.of(2023, 12, 31));
        when(projectRepository.findByIdAndStudent_UserId(projectId, userId)).thenReturn(Optional.of(existing));

        assertThatNoException().isThrownBy(() -> projectUseCase.delete(userId, projectId));

        verify(projectRepository).delete(existing);
    }

    @Test
    @DisplayName("should throw NotFoundException when project not found on delete")
    void shouldThrowWhenProjectNotFoundOnDelete() {
        when(projectRepository.findByIdAndStudent_UserId(projectId, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projectUseCase.delete(userId, projectId))
                .isInstanceOf(NotFoundException.class);

        verify(projectRepository, never()).delete(any());
    }

    @Test
    @DisplayName("should return paginated projects for the user")
    void shouldReturnPaginatedProjects() {
        Pageable pageable = PageRequest.of(0, 10);
        StudentProject project = buildProject(false, LocalDate.of(2023, 1, 1), LocalDate.of(2023, 12, 31));
        Page<StudentProject> page = new PageImpl<>(List.of(project), pageable, 1);

        when(projectRepository.findAllByStudent_UserId(userId, pageable)).thenReturn(page);

        PageResponse<ProjectResponse> response = projectUseCase.getAll(userId, pageable);

        assertThat(response.content()).hasSize(1);
        assertThat(response.content().get(0).title()).isEqualTo("UniHub Platform");
        assertThat(response.totalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("should return empty page when user has no projects")
    void shouldReturnEmptyPageWhenNoProjects() {
        Pageable pageable = PageRequest.of(0, 10);
        when(projectRepository.findAllByStudent_UserId(userId, pageable)).thenReturn(Page.empty(pageable));

        PageResponse<ProjectResponse> response = projectUseCase.getAll(userId, pageable);

        assertThat(response.content()).isEmpty();
    }

    @Test
    @DisplayName("should return single project by id")
    void shouldReturnSingleProjectById() {
        StudentProject project = buildProject(false, LocalDate.of(2023, 1, 1), LocalDate.of(2023, 12, 31));
        when(projectRepository.findByIdAndStudent_UserId(projectId, userId)).thenReturn(Optional.of(project));

        ProjectResponse response = projectUseCase.getOne(userId, projectId);

        assertThat(response.id()).isEqualTo(projectId);
        assertThat(response.title()).isEqualTo("UniHub Platform");
    }

    @Test
    @DisplayName("should throw NotFoundException when project not found by id")
    void shouldThrowWhenProjectNotFoundById() {
        when(projectRepository.findByIdAndStudent_UserId(projectId, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projectUseCase.getOne(userId, projectId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Project not found");
    }

    private ProjectRequest buildRequest(boolean current, LocalDate start, LocalDate end, Set<UUID> skillIds) {
        return new ProjectRequest(
                "UniHub Platform", "A university platform",
                start, end, current, "https://github.com/unihub", skillIds);
    }

    private StudentProject buildProject(boolean current, LocalDate start, LocalDate end) {
        StudentProject project = new StudentProject();
        ReflectionTestUtils.setField(project, "id", projectId);
        project.setStudent(profile);
        project.setTitle("UniHub Platform");
        project.setDescription("A university platform");
        project.setStartDate(start);
        project.setEndDate(end);
        project.setCurrent(current);
        project.setProjectLink("https://github.com/unihub");
        project.setSkills(new HashSet<>());
        return project;
    }
}