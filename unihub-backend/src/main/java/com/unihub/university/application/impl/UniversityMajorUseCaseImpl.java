package com.unihub.university.application.impl;

import com.unihub.shared.exception.InvalidOperationException;
import com.unihub.shared.exception.NotFoundException;
import com.unihub.university.api.dto.res.MajorResponse;
import com.unihub.university.application.usecase.UniversityMajorUseCase;
import com.unihub.university.domain.model.Major;
import com.unihub.university.domain.model.UniversityProfile;
import com.unihub.university.domain.repository.MajorRepository;
import com.unihub.university.domain.repository.UniversityProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UniversityMajorUseCaseImpl implements UniversityMajorUseCase {

    private final UniversityProfileRepository universityProfileRepository;
    private final MajorRepository majorRepository;


    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "majors:all")
    public List<MajorResponse> getAllAvailable() {
        return majorRepository.findAll()
                .stream()
                .map(m -> new MajorResponse(m.getId(), m.getName()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MajorResponse> getSelected(UUID userId) {
        UniversityProfile profile = getProfileByUserId(userId);
        return profile.getMajors()
                .stream()
                .map(m -> new MajorResponse(m.getId(), m.getName()))
                .toList();
    }

    @Override
    public void select(UUID userId, UUID majorId) {
        log.debug("Selecting major — userId={}, majorId={}", userId, majorId);

        UniversityProfile profile = getProfileByUserId(userId);
        Major major = majorRepository.findById(majorId)
                .orElseThrow(() -> new NotFoundException("Major not found"));

        if (!profile.getMajors().add(major)) {
            throw new InvalidOperationException("Major already selected.");
        }

        universityProfileRepository.save(profile);
        log.info("Major selected — userId={}, majorId={}", userId, majorId);
    }


    private UniversityProfile getProfileByUserId(UUID userId) {
        return universityProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("University profile not found"));
    }

}