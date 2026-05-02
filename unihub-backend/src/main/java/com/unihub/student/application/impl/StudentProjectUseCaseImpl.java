package com.unihub.student.application.impl;

import java.util.HashSet;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.unihub.shared.dto.PageResponse;
import com.unihub.shared.exception.NotFoundException;
import com.unihub.student.api.dto.ProjectRequest;
import com.unihub.student.api.dto.ProjectResponse;
import com.unihub.student.application.usecase.StudentProjectUseCase;
import com.unihub.student.domain.model.StudentProject;
import com.unihub.student.domain.repository.SkillRepository;
import com.unihub.student.domain.repository.StudentProfileRepository;
import com.unihub.student.domain.repository.StudentProjectRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class StudentProjectUseCaseImpl implements StudentProjectUseCase {

    private final StudentProfileRepository profileRepository;
    private final StudentProjectRepository projectRepository;
    private final SkillRepository skillRepository;

    public ProjectResponse add(UUID userId, ProjectRequest request) {
        var profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("Student profile not found"));

        var project = new StudentProject();
        project.setStudent(profile);
        mapRequest(project, request);

        return toResponse(projectRepository.save(project));
    }

    public ProjectResponse update(UUID userId, UUID projectId, ProjectRequest request) {
        var project = getOwnedProject(userId, projectId);
        mapRequest(project, request);
        return toResponse(projectRepository.save(project));
    }

    public void delete(UUID userId, UUID projectId) {
        projectRepository.delete(getOwnedProject(userId, projectId));
    }

    @Transactional(readOnly = true)
    public PageResponse<ProjectResponse> getAll(UUID userId, Pageable pageable) {
        return PageResponse.from(
                projectRepository.findAllByStudent_UserId(userId, pageable)
                        .map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public ProjectResponse getOne(UUID userId, UUID projectId) {
        return projectRepository.findByIdAndStudent_UserId(projectId, userId)
                .map(this::toResponse)
                .orElseThrow(() -> new NotFoundException("Project not found"));
    }

    private void mapRequest(StudentProject project, ProjectRequest request) {
        project.setTitle(request.title());
        project.setDescription(request.description());
        project.setStartDate(request.startDate());
        project.setEndDate(request.endDate());
        project.setProjectLink(request.projectLink());
        if (request.skillIds() != null) {
            project.setSkills(new HashSet<>(skillRepository.findAllByIdIn(request.skillIds())));
        }
    }

    private StudentProject getOwnedProject(UUID userId, UUID projectId) {
        var project = projectRepository.findById(projectId)
                .orElseThrow(() -> new NotFoundException("Project not found"));
        if (!project.getStudent().getUserId().equals(userId)) {
            throw new NotFoundException("Project not found");
        }
        return project;
    }

    private ProjectResponse toResponse(StudentProject project) {
        return new ProjectResponse(
                project.getId(),
                project.getTitle(),
                project.getDescription(),
                project.getStartDate(),
                project.getEndDate(),
                project.getProjectLink(),
                project.getSkills().stream().map(s -> s.getName()).toList());
    }
}
