package com.unihub.student.domain.repository;

import java.util.Optional;
import java.util.UUID;

import com.unihub.student.domain.model.GraduationCertificate;

public interface GraduationCertificateRepository {

    Optional<GraduationCertificate> findById(UUID certId);

    GraduationCertificate save(GraduationCertificate cert);

    Optional<GraduationCertificate> findTopByStudentIdOrderByAttemptNumberDesc(UUID studentId);

    int countByStudentId(UUID studentId);

}
