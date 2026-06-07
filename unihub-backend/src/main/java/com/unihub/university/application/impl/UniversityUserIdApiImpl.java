package com.unihub.university.application.impl;

import com.unihub.shared.api.external.UniversityUserIdApi;
import com.unihub.university.domain.model.UniversityProfile;
import com.unihub.university.domain.repository.UniversityProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UniversityUserIdApiImpl implements UniversityUserIdApi {

    private final UniversityProfileRepository universityProfileRepository;

    @Override
    @Transactional(readOnly = true)
    public Optional<UUID> findUserIdByUniversityProfileId(UUID universityProfileId) {
        return universityProfileRepository.findById(universityProfileId)
                .map(UniversityProfile::getUserId);
    }
}