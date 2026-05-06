package com.unihub.student.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.unihub.student.domain.model.GraduationCertificate;

public interface JpaGraduationCertificateRepository extends JpaRepository<GraduationCertificate, UUID> {
    Optional<GraduationCertificate> findTopByStudentIdOrderByAttemptNumberDesc(UUID studentId);

    int countByStudentId(UUID studentId);

    @Modifying
    @Query("DELETE FROM GraduationCertificate c WHERE c.studentId = :studentId")
    void deleteAllByStudentId(@Param("studentId") UUID studentId);
}
