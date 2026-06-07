package com.unihub.student.application.impl;

import com.unihub.shared.api.dto.PageResponse;
import com.unihub.shared.exception.NotFoundException;
import com.unihub.student.api.dto.req.ProjectRequest;
import com.unihub.student.api.dto.res.ProjectResponse;
import com.unihub.student.application.usecase.StudentProjectUseCase;
import com.unihub.student.domain.model.Skill;
import com.unihub.student.domain.model.StudentProfile;
import com.unihub.student.domain.model.StudentProject;
import com.unihub.student.domain.repository.SkillRepository;
import com.unihub.student.domain.repository.StudentProfileRepository;
import com.unihub.student.domain.repository.StudentProjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class StudentProjectUseCaseImpl implements StudentProjectUseCase {

    private final StudentProfileRepository profileRepository;
    private final StudentProjectRepository projectRepository;
    private final SkillRepository skillRepository;

    @Override
    public ProjectResponse add(UUID userId, ProjectRequest request) {
        log.debug("Adding project for userId={}, title={}", userId, request.title());

        StudentProfile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("Student profile not found"));

        StudentProject project = new StudentProject();
        project.setStudent(profile);
        mapRequest(project, request);

        ProjectResponse response = toResponse(projectRepository.save(project));
        log.info("Project added — userId={}, projectId={}", userId, response.id());
        return response;
    }

    @Override
    public ProjectResponse update(UUID userId, UUID projectId, ProjectRequest request) {
        log.debug("Updating project — userId={}, projectId={}", userId, projectId);

        StudentProject project = getOwnedProject(userId, projectId);
        mapRequest(project, request);

        ProjectResponse response = toResponse(projectRepository.save(project));
        log.info("Project updated — userId={}, projectId={}", userId, response.id());
        return response;
    }

    @Override
    public void delete(UUID userId, UUID projectId) {
        log.debug("Deleting project — userId={}, projectId={}", userId, projectId);

        projectRepository.delete(getOwnedProject(userId, projectId));

        log.info("Project deleted — userId={}, projectId={}", userId, projectId);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ProjectResponse> getAll(UUID userId, Pageable pageable) {
        return PageResponse.from(
                projectRepository.findAllByStudent_UserId(userId, pageable)
                        .map(this::toResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public ProjectResponse getOne(UUID userId, UUID projectId) {
        return toResponse(getOwnedProject(userId, projectId));
    }

    private void mapRequest(StudentProject project, ProjectRequest request) {

        validateProjectDates(request);
        project.setTitle(request.title());
        project.setDescription(request.description());
        project.setStartDate(request.startDate());
        project.setEndDate(request.endDate());
        project.setCurrent(request.current());
        project.setProjectLink(request.projectLink());
        if (request.skillIds() != null) {
            var skills = skillRepository.findAllByIdIn(request.skillIds());
            if (skills.size() != request.skillIds().size()) {
                throw new NotFoundException("One or more skills not found");
            }
            project.setSkills(new HashSet<>(skills));
        }
    }

    private StudentProject getOwnedProject(UUID userId, UUID projectId) {
        return projectRepository.findByIdAndStudent_UserId(projectId, userId)
                .orElseThrow(() -> {
                    log.warn("Unauthorized or missing project access — userId={}, projectId={}", userId, projectId);
                    return new NotFoundException("Project not found");
                });
    }

    private ProjectResponse toResponse(StudentProject project) {
        return new ProjectResponse(
                project.getId(),
                project.getTitle(),
                project.getDescription(),
                project.getStartDate(),
                project.getEndDate(),
                project.getCurrent(),
                project.getProjectLink(),
                project.getSkills().stream()
                        .map(Skill::getName)
                        .toList()
        );
    }

    private void validateProjectDates(ProjectRequest req) {

        boolean isCurrent = Boolean.TRUE.equals(req.current());

        if (isCurrent && req.endDate() != null) {
            log.warn("Validation failed — current project has endDate");
            throw new IllegalArgumentException("Current project cannot have end date");
        }

        if (!isCurrent && req.endDate() == null) {
            log.warn("Validation failed — non-current project missing endDate");
            throw new IllegalArgumentException("End date is required when project is not current");
        }

        if (req.endDate() != null && req.startDate().isAfter(req.endDate())) {
            log.warn("Validation failed — startDate={} is after endDate={}", req.startDate(), req.endDate());
            throw new IllegalArgumentException("Start date cannot be after end date");
        }
    }
}
