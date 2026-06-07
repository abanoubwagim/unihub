package com.unihub.university.domain.repository;

import com.unihub.university.domain.model.UniversityProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface UniversityProfileRepository {

    Optional<UniversityProfile> findById(UUID id);

    Optional<UniversityProfile> findByUserId(UUID userId);

    boolean existsByUserId(UUID userId);

    UniversityProfile save(UniversityProfile profile);

    void delete(UniversityProfile profile);

    List<UniversityProfile> findAllByIdIn(Set<UUID> ids);

    boolean existsByIdAndMajors_Id(UUID universityId, UUID majorId);

    boolean existsById(UUID universityProfileId);

    Page<UniversityProfile> findAll(Pageable pageable);
}