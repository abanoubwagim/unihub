package com.unihub.student.application.usecase;

import com.unihub.student.api.dto.req.UpdateProfileRequest;
import com.unihub.student.api.dto.res.GraduationCertResponse;
import com.unihub.student.api.dto.res.StudentProfileResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;
import java.util.UUID;

public interface StudentProfileUseCase {

    StudentProfileResponse updateProfile(UUID userId, UpdateProfileRequest request);

    void updateSkills(UUID userId, Set<UUID> skillIds);

    String uploadPhoto(UUID userId, MultipartFile file);

    GraduationCertResponse uploadGraduationCertificate(UUID userId, MultipartFile file);

    void reviewGraduationCertificate(UUID certId, UUID reviewerUniversityId, boolean approved, String rejectionReason);

    void setUniversityOnce(UUID userId, UUID universityId, UUID majorId);

}
