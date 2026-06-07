package com.unihub.university.application.impl;

import com.unihub.shared.api.dto.external.UniversityPublicInfo;
import com.unihub.shared.api.external.UniversityPublicApi;
import com.unihub.university.domain.model.UniversityProfile;
import com.unihub.university.domain.repository.UniversityProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UniversityPublicApiImpl implements UniversityPublicApi {

    private final UniversityProfileRepository universityProfileRepository;

    @Override
    public Optional<UniversityPublicInfo> getByUserId(UUID userId) {
        return universityProfileRepository.findByUserId(userId).map(this::toInfo);
    }

    @Override
    public Optional<UniversityPublicInfo> getByProfileId(UUID profileId) {
        return universityProfileRepository.findById(profileId).map(this::toInfo);
    }

    @Override
    public Map<UUID, UniversityPublicInfo> getByProfileIds(Set<UUID> profileIds) {
        if (profileIds == null || profileIds.isEmpty()) return Map.of();
        return universityProfileRepository.findAllByIdIn(profileIds).stream()
                .collect(Collectors.toMap(UniversityProfile::getId, this::toInfo));
    }

    @Override
    public boolean isMajorOfferedByUniversity(UUID universityId, UUID majorId) {
        return universityProfileRepository.existsByIdAndMajors_Id(universityId, majorId);
    }

    private UniversityPublicInfo toInfo(UniversityProfile p) {
        return new UniversityPublicInfo(
                p.getUserId(),
                p.getId(),
                p.getName(),
                p.getProfilePhotoUrl(),
                p.getCountryId(),
                p.getStudentCount(),
                p.getGraduateCount());
    }
}