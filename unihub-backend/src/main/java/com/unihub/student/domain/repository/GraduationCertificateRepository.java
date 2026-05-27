package com.unihub.student.domain.repository;

import com.unihub.student.domain.enums.GraduationCertificateStatus;
import com.unihub.student.domain.model.GraduationCertificate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface GraduationCertificateRepository {

    Optional<GraduationCertificate> findById(UUID certId);

    GraduationCertificate save(GraduationCertificate cert);

    Optional<GraduationCertificate> findTopByStudentIdOrderByAttemptNumberDesc(UUID studentId);

    int countByStudentId(UUID studentId);

    int countByStudentIdAndStatus(UUID studentId, GraduationCertificateStatus status);

    // Bulk delete — used during account deletion cleanup
    void deleteAllByStudentId(UUID studentId);

    Page<GraduationCertificate> findAllByUniversityIdAndStatus(
            UUID universityId, GraduationCertificateStatus status, Pageable pageable);
}
