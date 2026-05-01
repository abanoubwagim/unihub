package com.unihub.student.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.unihub.student.domain.model.StudentProject;

public interface JpaStudentProjectRepository extends JpaRepository<StudentProject, UUID> {
    Page<StudentProject> findAllByStudent_UserId(UUID userId, Pageable pageable);

    Optional<StudentProject> findByIdAndStudent_UserId(UUID id, UUID userId);

    Page<StudentProject> findAllByStudent_Id(UUID studentId, Pageable pageable);

    Optional<StudentProject> findByIdAndStudent_Id(UUID id, UUID studentId);
}