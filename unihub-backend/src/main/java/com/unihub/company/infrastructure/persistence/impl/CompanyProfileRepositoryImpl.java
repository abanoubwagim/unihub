package com.unihub.company.infrastructure.persistence.impl;

import com.unihub.company.domain.model.CompanyProfile;
import com.unihub.company.domain.repository.CompanyProfileRepository;
import com.unihub.company.infrastructure.persistence.jpa.JpaCompanyProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class CompanyProfileRepositoryImpl implements CompanyProfileRepository {

    private final JpaCompanyProfileRepository jpa;

    @Override
    public Optional<CompanyProfile> findById(UUID id) {
        return jpa.findById(id);
    }

    @Override
    public Optional<CompanyProfile> findByUserId(UUID userId) {
        return jpa.findByUserId(userId);
    }

    @Override
    public boolean existsByUserId(UUID userId) {
        return jpa.existsByUserId(userId);
    }

    @Override
    public CompanyProfile save(CompanyProfile profile) {
        return jpa.save(profile);
    }

    @Override
    public void delete(CompanyProfile profile) {
        jpa.delete(profile);
    }

    @Override
    public List<CompanyProfile> findAllByIdIn(Set<UUID> ids) {
        if (ids == null || ids.isEmpty()) return List.of();
        return jpa.findAllByIdIn(ids);
    }
}