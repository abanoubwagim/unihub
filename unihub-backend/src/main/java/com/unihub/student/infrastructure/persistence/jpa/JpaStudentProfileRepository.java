package com.unihub.student.infrastructure.persistence.jpa;

import com.unihub.student.domain.model.StudentProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface JpaStudentProfileRepository extends JpaRepository<StudentProfile, UUID> {

    @EntityGraph(attributePaths = {"skills", "links"})
    Optional<StudentProfile> findByUserId(UUID userId);

    @EntityGraph(attributePaths = {"skills", "links"})
    Optional<StudentProfile> findById(UUID id);

    boolean existsByUserId(UUID userId);

    @EntityGraph(attributePaths = {"skills", "links"})
    List<StudentProfile> findAllByUserIdIn(Set<UUID> userIds);

    List<StudentProfile> findAllByIdIn(Set<UUID> ids);

    @EntityGraph(attributePaths = {"skills", "links"})
    Page<StudentProfile> findAllByUniversityId(UUID universityId, Pageable pageable);

}
