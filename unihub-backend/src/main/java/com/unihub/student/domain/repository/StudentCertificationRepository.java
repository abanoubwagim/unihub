package com.unihub.student.domain.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.unihub.student.domain.model.StudentCertification;

public interface StudentCertificationRepository {

    Optional<StudentCertification> findById(UUID id);

    StudentCertification save(StudentCertification entity);

    void delete(StudentCertification entity);

    // For authenticated student (by userId)
    Page<StudentCertification> findAllByStudent_UserId(UUID userId, Pageable pageable);

    Optional<StudentCertification> findByIdAndStudent_UserId(UUID id, UUID userId);

    // For public profile (by studentProfileId)
    Page<StudentCertification> findAllByStudent_Id(UUID studentId, Pageable pageable);

    Optional<StudentCertification> findByIdAndStudent_Id(UUID id, UUID studentId);
}