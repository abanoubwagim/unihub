package com.unihub.university.infrastructure.persistence.impl;

import com.unihub.university.domain.model.UniversityPartnership;
import com.unihub.university.domain.repository.UniversityPartnershipRepository;
import com.unihub.university.infrastructure.persistence.jpa.JpaUniversityPartnershipRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class UniversityPartnershipRepositoryImpl implements UniversityPartnershipRepository {

    private final JpaUniversityPartnershipRepository jpa;

    @Override
    public Optional<UniversityPartnership> findById(UUID id) {
        return jpa.findById(id);
    }

    @Override
    public Optional<UniversityPartnership> findByIdAndUniversityId(UUID id, UUID universityId) {
        return jpa.findByIdAndUniversityId(id, universityId);
    }

    @Override
    public Optional<UniversityPartnership> findByIdAndCompanyId(UUID id, UUID companyId) {
        return jpa.findByIdAndCompanyId(id, companyId);
    }

    @Override
    public Page<UniversityPartnership> findAllByUniversityId(UUID universityId, Pageable pageable) {
        return jpa.findAllByUniversityId(universityId, pageable);
    }

    @Override
    public boolean existsByUniversityIdAndCompanyId(UUID universityId, UUID companyId) {
        return jpa.existsByUniversityIdAndCompanyId(universityId, companyId);
    }

    @Override
    public Page<UniversityPartnership> findAllByCompanyId(UUID companyId, Pageable pageable) {
        return jpa.findAllByCompanyId(companyId, pageable);
    }


    @Override
    public UniversityPartnership save(UniversityPartnership partnership) {
        return jpa.save(partnership);
    }

    @Override
    public List<UUID> findActivePartnerCompanyIds(UUID universityProfileId) {
        return jpa.findActivePartnerCompanyIds(universityProfileId);
    }
}