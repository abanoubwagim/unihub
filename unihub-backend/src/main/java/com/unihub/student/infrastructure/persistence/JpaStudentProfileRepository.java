package com.unihub.student.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.unihub.student.domain.model.StudentProfile;

public interface JpaStudentProfileRepository extends JpaRepository<StudentProfile, UUID> {
    
    @EntityGraph(attributePaths = {"skills", "links"})
    Optional<StudentProfile> findByUserId(UUID userId);

    @EntityGraph(attributePaths = {"skills", "links"})
    Optional<StudentProfile> findById(UUID id);
    
    boolean existsByUserId(UUID userId);
}
