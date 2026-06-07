package com.unihub.university.infrastructure.persistence.impl;

import com.unihub.university.domain.model.UniversityProfile;
import com.unihub.university.domain.repository.UniversityProfileRepository;
import com.unihub.university.infrastructure.persistence.jpa.JpaUniversityProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class UniversityProfileRepositoryImpl implements UniversityProfileRepository {

    private final JpaUniversityProfileRepository jpa;

    @Override
    public Optional<UniversityProfile> findById(UUID id) {
        return jpa.findById(id);
    }

    @Override
    public Optional<UniversityProfile> findByUserId(UUID userId) {
        return jpa.findByUserId(userId);
    }

    @Override
    public boolean existsByUserId(UUID userId) {
        return jpa.existsByUserId(userId);
    }

    @Override
    public UniversityProfile save(UniversityProfile profile) {
        return jpa.save(profile);
    }

    @Override
    public void delete(UniversityProfile profile) {
        jpa.delete(profile);
    }

    @Override
    public List<UniversityProfile> findAllByIdIn(Set<UUID> ids) {
        if (ids == null || ids.isEmpty()) return List.of();
        return jpa.findAllByIdIn(ids);
    }

    @Override
    public boolean existsByIdAndMajors_Id(UUID universityId, UUID majorId) {
        return jpa.existsByIdAndMajors_Id(universityId, majorId);
    }

    @Override
    public boolean existsById(UUID universityProfileId) {
        return jpa.existsById(universityProfileId);
    }

    @Override
    public Page<UniversityProfile> findAll(Pageable pageable) {
        return jpa.findAll(pageable);
    }
}