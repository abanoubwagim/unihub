package com.unihub.university.infrastructure.persistence.jpa;

import com.unihub.university.domain.model.UniversityPartnership;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JpaUniversityPartnershipRepository extends JpaRepository<UniversityPartnership, UUID> {

    Optional<UniversityPartnership> findByIdAndUniversityId(UUID id, UUID universityId);

    Optional<UniversityPartnership> findByIdAndCompanyId(UUID id, UUID companyId);

    Page<UniversityPartnership> findAllByUniversityId(UUID universityId, Pageable pageable);

    Page<UniversityPartnership> findAllByCompanyId(UUID companyId, Pageable pageable);

    boolean existsByUniversityIdAndCompanyId(UUID universityId, UUID companyId);

    @Query("SELECT p.companyId FROM UniversityPartnership p " +
            "WHERE p.universityId = :universityId AND p.status = 'ACTIVE'")
    List<UUID> findActivePartnerCompanyIds(@Param("universityId") UUID universityId);
}