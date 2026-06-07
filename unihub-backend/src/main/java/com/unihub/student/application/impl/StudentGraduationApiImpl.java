package com.unihub.student.application.impl;

import com.unihub.shared.api.dto.external.PendingCertInfo;
import com.unihub.shared.api.external.StudentGraduationApi;
import com.unihub.student.application.usecase.StudentProfileUseCase;
import com.unihub.student.domain.enums.GraduationCertificateStatus;
import com.unihub.student.domain.model.GraduationCertificate;
import com.unihub.student.domain.model.StudentProfile;
import com.unihub.student.domain.repository.GraduationCertificateRepository;
import com.unihub.student.domain.repository.StudentProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class StudentGraduationApiImpl implements StudentGraduationApi {

    private final GraduationCertificateRepository gradCertRepo;
    private final StudentProfileUseCase studentProfileUseCase;
    private final StudentProfileRepository studentProfileRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<PendingCertInfo> getPendingCertificates(UUID universityProfileId, Pageable pageable) {
        Page<GraduationCertificate> certPage = gradCertRepo
                .findAllByUniversityIdAndStatus(universityProfileId, GraduationCertificateStatus.PENDING, pageable);

        Set<UUID> studentIds = certPage.getContent().stream()
                .map(GraduationCertificate::getStudentId)
                .collect(Collectors.toSet());

        Map<UUID, StudentProfile> profileMap = studentProfileRepository
                .findAllByIdIn(studentIds)
                .stream()
                .collect(Collectors.toMap(StudentProfile::getId, p -> p));

        return certPage.map(cert -> {
            StudentProfile profile = profileMap.get(cert.getStudentId());
            return new PendingCertInfo(
                    cert.getId(),
                    cert.getStudentId(),
                    profile != null ? profile.getName() : null,
                    profile != null ? profile.getProfilePhotoUrl() : null,
                    cert.getFileUrl(),
                    cert.getAttemptNumber(),
                    cert.getSubmittedAt()
            );
        });
    }

    @Override
    public void reviewCertificate(UUID certId, UUID reviewerUniversityId, boolean approved, String rejectionReason) {
        studentProfileUseCase.reviewGraduationCertificate(certId, reviewerUniversityId, approved, rejectionReason);
    }
}