package com.unihub.student.domain.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.unihub.student.domain.model.StudentExperience;

public interface StudentExperienceRepository {

    Optional<StudentExperience> findById(UUID id);

    StudentExperience save(StudentExperience entity);

    void delete(StudentExperience entity);

    // For authenticated student (by userId)
    Page<StudentExperience> findAllByStudent_UserId(UUID userId, Pageable pageable);

    Optional<StudentExperience> findByIdAndStudent_UserId(UUID id, UUID userId);

    // For public profile (by studentProfileId)
    Page<StudentExperience> findAllByStudent_Id(UUID studentId, Pageable pageable);

    Optional<StudentExperience> findByIdAndStudent_Id(UUID id, UUID studentId);
}