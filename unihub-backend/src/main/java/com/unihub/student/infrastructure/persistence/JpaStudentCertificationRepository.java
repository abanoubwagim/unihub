package com.unihub.student.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.unihub.student.domain.model.StudentCertification;

public interface JpaStudentCertificationRepository extends JpaRepository<StudentCertification, UUID> {
    Page<StudentCertification> findAllByStudent_UserId(UUID userId, Pageable pageable);

    Optional<StudentCertification> findByIdAndStudent_UserId(UUID id, UUID userId);

    Page<StudentCertification> findAllByStudent_Id(UUID studentId, Pageable pageable);

    Optional<StudentCertification> findByIdAndStudent_Id(UUID id, UUID studentId);

    @Modifying
    @Query("DELETE FROM StudentCertification c WHERE c.student.id = :studentId")
    void deleteAllByStudent_Id(@Param("studentId") UUID studentId);
}
