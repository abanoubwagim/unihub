package com.unihub.student.application.impl;

import com.unihub.student.application.PendingCertInfo;
import com.unihub.student.application.StudentGraduationApi;
import com.unihub.student.application.usecase.StudentProfileUseCase;
import com.unihub.student.domain.enums.GraduationCertificateStatus;
import com.unihub.student.domain.repository.GraduationCertificateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudentGraduationApiImpl implements StudentGraduationApi {

    private final GraduationCertificateRepository gradCertRepo;
    private final StudentProfileUseCase studentProfileUseCase;

    @Override
    public Page<PendingCertInfo> getPendingCertificates(UUID universityProfileId, Pageable pageable) {
        return gradCertRepo
                .findAllByUniversityIdAndStatus(universityProfileId, GraduationCertificateStatus.PENDING, pageable)
                .map(c -> new PendingCertInfo(
                        c.getId(),
                        c.getStudentId(),
                        c.getFileUrl(),
                        c.getAttemptNumber(),
                        c.getSubmittedAt()));
    }

    @Override
    @Transactional
    public void reviewCertificate(UUID certId, UUID reviewerUniversityId, boolean approved, String rejectionReason) {
        studentProfileUseCase.reviewGraduationCertificate(certId, reviewerUniversityId, approved, rejectionReason);
    }
}