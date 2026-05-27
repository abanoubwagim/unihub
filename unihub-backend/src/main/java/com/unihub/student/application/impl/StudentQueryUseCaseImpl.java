package com.unihub.student.application.impl;

import com.unihub.shared.exception.NotFoundException;
import com.unihub.student.api.dto.res.*;
import com.unihub.student.application.StudentProfileMapper;
import com.unihub.student.application.usecase.StudentQueryUseCase;
import com.unihub.student.domain.enums.GraduationCertificateStatus;
import com.unihub.student.domain.model.StudentCertification;
import com.unihub.student.domain.model.StudentExperience;
import com.unihub.student.domain.model.StudentProject;
import com.unihub.student.domain.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudentQueryUseCaseImpl implements StudentQueryUseCase {

    private static final int PUBLIC_PAGE_SIZE = 50;

    private final StudentProfileRepository studentProfileRepository;
    private final GraduationCertificateRepository gradCertRepo;
    private final StudentExperienceRepository experienceRepository;
    private final StudentProjectRepository projectRepository;
    private final StudentCertificationRepository certificationRepository;
    private final StudentProfileMapper mapper;

    @Override
    public StudentProfileResponse getMyProfile(UUID userId) {
        return studentProfileRepository.findByUserId(userId)
                .map(mapper::toResponse)
                .orElseThrow(() -> new NotFoundException("Student profile not found"));
    }

    @Override
    public StudentProfileResponse getPublicProfile(UUID studentId) {
        return studentProfileRepository.findByUserId(studentId)
                .map(mapper::toResponse)
                .orElseThrow(() -> new NotFoundException("Student not found"));
    }

    @Override
    public GraduationCertResponse getGradCertStatus(UUID userId) {
        var profile = studentProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("Student profile not found"));

        return gradCertRepo.findTopByStudentIdOrderByAttemptNumberDesc(profile.getId())
                .map(cert -> new GraduationCertResponse(
                        cert.getId(),
                        cert.getStatus(),
                        cert.getAttemptNumber(),
                        cert.getRejectionReason()))
                .orElse(new GraduationCertResponse(
                        null,
                        GraduationCertificateStatus.NOT_SUBMITTED,
                        0,
                        null));
    }

    @Override
    public List<ExperienceResponse> getExperiences(UUID studentId) {
        validateStudentExists(studentId);
        Pageable pageable = PageRequest.of(0, PUBLIC_PAGE_SIZE, Sort.by("startDate").descending());
        return experienceRepository.findAllByStudent_Id(studentId, pageable)
                .getContent()
                .stream()
                .map(this::mapExperience)
                .toList();
    }

    @Override
    public ExperienceResponse getExperience(UUID studentId, UUID experienceId) {
        validateStudentExists(studentId);
        return experienceRepository.findByIdAndStudent_Id(experienceId, studentId)
                .map(this::mapExperience)
                .orElseThrow(() -> new NotFoundException("Experience not found"));
    }

    @Override
    public List<ProjectResponse> getProjects(UUID studentId) {
        validateStudentExists(studentId);
        Pageable pageable = PageRequest.of(0, PUBLIC_PAGE_SIZE, Sort.by("startDate").descending());
        return projectRepository.findAllByStudent_Id(studentId, pageable)
                .getContent()
                .stream()
                .map(this::mapProject)
                .toList();
    }

    @Override
    public ProjectResponse getProject(UUID studentId, UUID projectId) {
        validateStudentExists(studentId);
        return projectRepository.findByIdAndStudent_Id(projectId, studentId)
                .map(this::mapProject)
                .orElseThrow(() -> new NotFoundException("Project not found"));
    }

    @Override
    public List<CertificationResponse> getCertifications(UUID studentId) {
        validateStudentExists(studentId);
        Pageable pageable = PageRequest.of(0, PUBLIC_PAGE_SIZE, Sort.by("dateIssued").descending());
        return certificationRepository.findAllByStudent_Id(studentId, pageable)
                .getContent()
                .stream()
                .map(this::mapCertification)
                .toList();
    }

    @Override
    public CertificationResponse getCertification(UUID studentId, UUID certId) {
        validateStudentExists(studentId);
        return certificationRepository.findByIdAndStudent_Id(certId, studentId)
                .map(this::mapCertification)
                .orElseThrow(() -> new NotFoundException("Certification not found"));
    }

    private void validateStudentExists(UUID studentId) {
        if (!studentProfileRepository.existsByUserId(studentId)) {
            throw new NotFoundException("Student not found");
        }
    }

    private ExperienceResponse mapExperience(StudentExperience exp) {
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
                exp.getSkills().stream().map(s -> s.getName()).toList());
    }

    private ProjectResponse mapProject(StudentProject p) {
        return new ProjectResponse(
                p.getId(),
                p.getTitle(),
                p.getDescription(),
                p.getStartDate(),
                p.getEndDate(),
                p.getCurrent(),
                p.getProjectLink(),
                p.getSkills().stream().map(s -> s.getName()).toList());
    }

    private CertificationResponse mapCertification(StudentCertification c) {
        return new CertificationResponse(
                c.getId(),
                c.getTitle(),
                c.getIssuingOrganization(),
                c.getDateIssued(),
                c.getFileUrl());
    }
}