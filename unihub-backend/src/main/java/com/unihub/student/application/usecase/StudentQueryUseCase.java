package com.unihub.student.application.usecase;

import java.util.List;
import java.util.UUID;

import com.unihub.student.api.dto.res.CertificationResponse;
import com.unihub.student.api.dto.res.ExperienceResponse;
import com.unihub.student.api.dto.res.GraduationCertResponse;
import com.unihub.student.api.dto.res.ProjectResponse;
import com.unihub.student.api.dto.res.StudentProfileResponse;

public interface StudentQueryUseCase {

    StudentProfileResponse getMyProfile(UUID userId);

    StudentProfileResponse getPublicProfile(UUID studentId);

    GraduationCertResponse getGradCertStatus(UUID userId);

    List<ExperienceResponse> getExperiences(UUID studentId);

    ExperienceResponse getExperience(UUID studentId, UUID experienceId);

    List<ProjectResponse> getProjects(UUID studentId);

    ProjectResponse getProject(UUID studentId, UUID projectId);

    List<CertificationResponse> getCertifications(UUID studentId);

    CertificationResponse getCertification(UUID studentId, UUID certId);
}
