package com.unihub.student.infrastructure.persistence.jpa;

import com.unihub.student.domain.enums.GraduationCertificateStatus;
import com.unihub.student.domain.model.GraduationCertificate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface JpaGraduationCertificateRepository extends JpaRepository<GraduationCertificate, UUID> {
    Optional<GraduationCertificate> findTopByStudentIdOrderByAttemptNumberDesc(UUID studentId);

    int countByStudentIdAndStatus(UUID studentId, GraduationCertificateStatus status);

    @Modifying
    @Query("DELETE FROM GraduationCertificate c WHERE c.studentId = :studentId")
    void deleteAllByStudentId(@Param("studentId") UUID studentId);

    Page<GraduationCertificate> findAllByUniversityIdAndStatus(
            UUID universityId, GraduationCertificateStatus status, Pageable pageable);
}
