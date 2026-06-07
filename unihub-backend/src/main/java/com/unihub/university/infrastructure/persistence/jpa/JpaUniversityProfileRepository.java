package com.unihub.university.infrastructure.persistence.jpa;

import com.unihub.university.domain.model.UniversityProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface JpaUniversityProfileRepository extends JpaRepository<UniversityProfile, UUID> {

    @EntityGraph(attributePaths = {"majors"})
    Optional<UniversityProfile> findByUserId(UUID userId);

    @EntityGraph(attributePaths = {"majors"})
    Optional<UniversityProfile> findById(UUID id);

    boolean existsByUserId(UUID userId);

    @EntityGraph(attributePaths = {"majors"})
    List<UniversityProfile> findAllByIdIn(Set<UUID> ids);

    boolean existsByIdAndMajors_Id(UUID universityId, UUID majorId);

    @EntityGraph(attributePaths = {"majors"})
    Page<UniversityProfile> findAll(Pageable pageable);
}