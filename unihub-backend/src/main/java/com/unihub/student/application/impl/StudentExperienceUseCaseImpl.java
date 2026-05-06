package com.unihub.student.application.impl;

import java.util.HashSet;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.unihub.shared.dto.PageResponse;
import com.unihub.shared.exception.NotFoundException;
import com.unihub.student.api.dto.ExperienceRequest;
import com.unihub.student.api.dto.ExperienceResponse;
import com.unihub.student.application.usecase.StudentExperienceUseCase;
import com.unihub.student.domain.model.StudentExperience;
import com.unihub.student.domain.repository.SkillRepository;
import com.unihub.student.domain.repository.StudentExperienceRepository;
import com.unihub.student.domain.repository.StudentProfileRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class StudentExperienceUseCaseImpl implements StudentExperienceUseCase {

    private final StudentProfileRepository profileRepository;
    private final StudentExperienceRepository experienceRepository;
    private final SkillRepository skillRepository;

    public ExperienceResponse add(UUID userId, ExperienceRequest request) {
        var profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("Student profile not found"));

        var experience = new StudentExperience();
        experience.setStudent(profile);
        mapRequest(experience, request);

        return toResponse(experienceRepository.save(experience));
    }

    public ExperienceResponse update(UUID userId, UUID experienceId, ExperienceRequest request) {
        var experience = getOwnedExperience(userId, experienceId);
        mapRequest(experience, request);
        return toResponse(experienceRepository.save(experience));
    }

    public void delete(UUID userId, UUID experienceId) {
        var experience = getOwnedExperience(userId, experienceId);
        experienceRepository.delete(experience);
    }

    @Transactional(readOnly = true)
    public PageResponse<ExperienceResponse> getAll(UUID userId, Pageable pageable) {
        return PageResponse.from(
                experienceRepository.findAllByStudent_UserId(userId, pageable)
                        .map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public ExperienceResponse getOne(UUID userId, UUID experienceId) {
        return experienceRepository.findByIdAndStudent_UserId(experienceId, userId)
                .map(this::toResponse)
                .orElseThrow(() -> new NotFoundException("Experience not found"));
    }

    private void mapRequest(StudentExperience exp, ExperienceRequest req) {
        exp.setJobTitle(req.jobTitle());
        exp.setCompany(req.company());
        exp.setJobType(req.jobType());
        exp.setStartDate(req.startDate());
        exp.setEndDate(req.endDate());
        exp.setCurrent(req.current());
        exp.setLocation(req.location());
        exp.setDescription(req.description());
        if (req.skillIds() != null) {
            exp.setSkills(new HashSet<>(skillRepository.findAllByIdIn(req.skillIds())));
        }
    }

    private StudentExperience getOwnedExperience(UUID userId, UUID experienceId) {
        return experienceRepository.findByIdAndStudent_UserId(experienceId, userId)
                .orElseThrow(() -> new NotFoundException("Experience not found"));
    }

    private ExperienceResponse toResponse(StudentExperience exp) {
        return new ExperienceResponse(
                exp.getId(),
                exp.getJobTitle(),
                exp.getCompany(),
                exp.getJobType(),
                exp.getStartDate(),
                exp.getEndDate(),
                exp.isCurrent(),
                exp.getLocation(),
                exp.getDescription(),
                exp.getSkills().stream().map(s -> s.getName()).toList());
    }
}
