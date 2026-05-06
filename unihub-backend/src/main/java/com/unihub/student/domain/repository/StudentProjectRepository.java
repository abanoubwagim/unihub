package com.unihub.student.domain.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.unihub.student.domain.model.StudentProject;

public interface StudentProjectRepository {

    Optional<StudentProject> findById(UUID id);

    StudentProject save(StudentProject entity);

    void delete(StudentProject entity);

    // For authenticated student (by userId)
    Page<StudentProject> findAllByStudent_UserId(UUID userId, Pageable pageable);

    Optional<StudentProject> findByIdAndStudent_UserId(UUID id, UUID userId);

    // For public profile (by studentProfileId)
    Page<StudentProject> findAllByStudent_Id(UUID studentId, Pageable pageable);

    Optional<StudentProject> findByIdAndStudent_Id(UUID id, UUID studentId);

    // Bulk delete — used during account deletion cleanup
    void deleteAllByStudent_Id(UUID studentId);
}