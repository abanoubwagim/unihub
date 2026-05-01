package com.unihub.student.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;

import com.unihub.student.domain.model.StudentExperience;

public interface JpaStudentExperienceRepository extends JpaRepository<StudentExperience, UUID> {
    Page<StudentExperience> findAllByStudent_UserId(UUID userId, Pageable pageable);

    Optional<StudentExperience> findByIdAndStudent_UserId(UUID id, UUID userId);

    Page<StudentExperience> findAllByStudent_Id(UUID studentId, Pageable pageable);

    Optional<StudentExperience> findByIdAndStudent_Id(UUID id, UUID studentId);
}
