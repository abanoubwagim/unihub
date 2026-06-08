package com.unihub.company.domain.repository;

import com.unihub.company.domain.model.CompanyProfile;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface CompanyProfileRepository {

    Optional<CompanyProfile> findById(UUID id);

    Optional<CompanyProfile> findByUserId(UUID userId);

    boolean existsByUserId(UUID userId);

    CompanyProfile save(CompanyProfile profile);

    void delete(CompanyProfile profile);

    List<CompanyProfile> findAllByIdIn(Set<UUID> ids);
}