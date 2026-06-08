package com.unihub.company.infrastructure.persistence.jpa;

import com.unihub.company.domain.model.CompanyProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface JpaCompanyProfileRepository extends JpaRepository<CompanyProfile, UUID> {

    Optional<CompanyProfile> findByUserId(UUID userId);

    boolean existsByUserId(UUID userId);

    List<CompanyProfile> findAllByIdIn(Set<UUID> ids);
}