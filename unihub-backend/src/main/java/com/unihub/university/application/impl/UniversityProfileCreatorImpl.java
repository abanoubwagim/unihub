package com.unihub.university.application.impl;

import com.unihub.university.application.UniversityProfileCreator;
import com.unihub.university.domain.model.UniversityProfile;
import com.unihub.university.domain.repository.UniversityProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UniversityProfileCreatorImpl implements UniversityProfileCreator {

    private final UniversityProfileRepository universityProfileRepository;

    @Override
    @Transactional
    public void createEmptyProfile(UUID userId) {
        if (universityProfileRepository.existsByUserId(userId)) {
            log.debug("University profile already exists for userId={}, skipping creation", userId);
            return;
        }
        try {
            UniversityProfile profile = new UniversityProfile();
            profile.setUserId(userId);
            universityProfileRepository.save(profile);
            log.info("Created empty university profile for userId={}", userId);
        } catch (DataIntegrityViolationException e) {
            log.warn("Concurrent university profile creation detected for userId={} — already exists", userId);
        }
    }
}