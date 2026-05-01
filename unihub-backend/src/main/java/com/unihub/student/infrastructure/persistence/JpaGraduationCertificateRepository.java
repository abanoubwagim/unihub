package com.unihub.student.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.unihub.student.domain.model.GraduationCertificate;

public interface JpaGraduationCertificateRepository extends JpaRepository<GraduationCertificate, UUID> {
    Optional<GraduationCertificate> findTopByStudentIdOrderByAttemptNumberDesc(UUID studentId);

    int countByStudentId(UUID studentId);

}
