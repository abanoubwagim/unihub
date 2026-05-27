package com.unihub.student.application.impl;

import com.unihub.shared.dto.PageResponse;
import com.unihub.shared.exception.NotFoundException;
import com.unihub.student.api.dto.req.ExperienceRequest;
import com.unihub.student.api.dto.res.ExperienceResponse;
import com.unihub.student.application.usecase.StudentExperienceUseCase;
import com.unihub.student.domain.model.Skill;
import com.unihub.student.domain.model.StudentExperience;
import com.unihub.student.domain.model.StudentProfile;
import com.unihub.student.domain.repository.SkillRepository;
import com.unihub.student.domain.repository.StudentExperienceRepository;
import com.unihub.student.domain.repository.StudentProfileRepository;
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
public class StudentExperienceUseCaseImpl implements StudentExperienceUseCase {

    private final StudentProfileRepository profileRepository;
    private final StudentExperienceRepository experienceRepository;
    private final SkillRepository skillRepository;

    @Override
    public ExperienceResponse add(UUID userId, ExperienceRequest request) {
        log.debug("Adding experience for userId={}, jobTitle={}", userId, request.jobTitle());

        StudentProfile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("Student profile not found"));

        StudentExperience experience = new StudentExperience();
        experience.setStudent(profile);
        mapRequest(experience, request);

        ExperienceResponse response = toResponse(experienceRepository.save(experience));
        log.info("Experience added — userId={}, experienceId={}", userId, response.id());
        return response;
    }

    @Override
    public ExperienceResponse update(UUID userId, UUID experienceId, ExperienceRequest request) {
        log.debug("Updating experience — userId={}, experienceId={}", userId, experienceId);

        StudentExperience experience = getOwnedExperience(userId, experienceId);
        mapRequest(experience, request);

        ExperienceResponse response = toResponse(experienceRepository.save(experience));
        log.info("Experience updated — userId={}, experienceId={}", userId, experienceId);
        return response;
    }

    @Override
    public void delete(UUID userId, UUID experienceId) {
        log.debug("Deleting experience — userId={}, experienceId={}", userId, experienceId);

        var experience = getOwnedExperience(userId, experienceId);
        experienceRepository.delete(experience);
        log.info("Experience deleted — userId={}, experienceId={}", userId, experienceId);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ExperienceResponse> getAll(UUID userId, Pageable pageable) {
        return PageResponse.from(
                experienceRepository.findAllByStudent_UserId(userId, pageable)
                        .map(this::toResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public ExperienceResponse getOne(UUID userId, UUID experienceId) {
        return toResponse(getOwnedExperience(userId, experienceId));
    }

    private StudentExperience getOwnedExperience(UUID userId, UUID experienceId) {
        return experienceRepository.findByIdAndStudent_UserId(experienceId, userId)
                .orElseThrow(() -> {
                    log.warn("Unauthorized or missing experience access — userId={}, experienceId={}", userId, experienceId);
                    return new NotFoundException("Experience not found");
                });
    }

    private void mapRequest(StudentExperience exp, ExperienceRequest req) {

        validateExperienceDates(req);
        exp.setJobTitle(req.jobTitle());
        exp.setCompany(req.company());
        exp.setJobType(req.jobType());
        exp.setStartDate(req.startDate());
        exp.setEndDate(req.endDate());
        exp.setCurrent(req.current());
        exp.setLocation(req.location());
        exp.setDescription(req.description());
        if (req.skillIds() != null) {
            var skills = skillRepository.findAllByIdIn(req.skillIds());
            if (skills.size() != req.skillIds().size()) {
                throw new NotFoundException("One or more skills not found");
            }
            exp.setSkills(new HashSet<>(skills));
        }
    }

    private ExperienceResponse toResponse(StudentExperience exp) {
        return new ExperienceResponse(
                exp.getId(),
                exp.getJobTitle(),
                exp.getCompany(),
                exp.getJobType(),
                exp.getStartDate(),
                exp.getEndDate(),
                exp.getCurrent(),
                exp.getLocation(),
                exp.getDescription(),
                exp.getSkills().stream()
                        .map(Skill::getName)
                        .toList()
        );
    }

    private void validateExperienceDates(ExperienceRequest req) {

        boolean isCurrent = Boolean.TRUE.equals(req.current());

        if (isCurrent && req.endDate() != null) {
            log.warn("Validation failed — current experience has endDate");
            throw new IllegalArgumentException("Current experience cannot have end date");
        }

        if (!isCurrent && req.endDate() == null) {
            log.warn("Validation failed — non-current experience missing endDate");
            throw new IllegalArgumentException("End date is required when experience is not current");
        }

        if (req.endDate() != null && req.startDate().isAfter(req.endDate())) {
            log.warn("Validation failed — startDate={} is after endDate={}", req.startDate(), req.endDate());
            throw new IllegalArgumentException("Start date cannot be after end date");
        }
    }
}
