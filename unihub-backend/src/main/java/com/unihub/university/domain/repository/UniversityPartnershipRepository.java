package com.unihub.university.domain.repository;

import com.unihub.university.domain.model.UniversityPartnership;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


public interface UniversityPartnershipRepository {

    Optional<UniversityPartnership> findById(UUID id);

    Optional<UniversityPartnership> findByIdAndUniversityId(UUID id, UUID universityId);

    Optional<UniversityPartnership> findByIdAndCompanyId(UUID id, UUID companyId);

    Page<UniversityPartnership> findAllByCompanyId(UUID companyId, Pageable pageable);

    Page<UniversityPartnership> findAllByUniversityId(UUID universityId, Pageable pageable);

    boolean existsByUniversityIdAndCompanyId(UUID universityId, UUID companyId);

    UniversityPartnership save(UniversityPartnership partnership);

    List<UUID> findActivePartnerCompanyIds(UUID universityProfileId);

}