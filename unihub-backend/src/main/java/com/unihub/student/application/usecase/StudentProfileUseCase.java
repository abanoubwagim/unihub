package com.unihub.student.application.usecase;

import java.util.Set;
import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;

import com.unihub.student.api.dto.GraduationCertResponse;
import com.unihub.student.api.dto.StudentProfileResponse;
import com.unihub.student.api.dto.UpdateProfileRequest;

public interface StudentProfileUseCase {
    
    StudentProfileResponse updateProfile(UUID userId, UpdateProfileRequest request);

    void setUniversity(UUID userId, UUID universityId, UUID majorId);

    void updateSkills(UUID userId, Set<UUID> skillIds);

    String uploadPhoto(UUID userId, MultipartFile file);

    GraduationCertResponse uploadGraduationCertificate(UUID userId, MultipartFile file);

    void reviewGraduationCertificate(UUID certId, boolean approved, String rejectionReason);
}
