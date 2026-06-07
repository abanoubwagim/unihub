package com.unihub.university.application.impl;

import com.unihub.shared.exception.NotFoundException;
import com.unihub.shared.storage.FileStorageService;
import com.unihub.university.api.dto.req.UpdateProfileRequest;
import com.unihub.university.api.dto.res.UniversityProfileResponse;
import com.unihub.university.application.UniversityProfileMapper;
import com.unihub.university.application.usecase.UniversityProfileUseCase;
import com.unihub.university.domain.model.UniversityProfile;
import com.unihub.university.domain.repository.UniversityProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UniversityProfileUseCaseImpl implements UniversityProfileUseCase {

    private final UniversityProfileRepository universityProfileRepository;
    private final FileStorageService fileStorageService;
    private final UniversityProfileMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public UniversityProfileResponse getMyProfile(UUID userId) {
        return mapper.toResponse(getProfileByUserId(userId));
    }

    @Override
    public UniversityProfileResponse updateProfile(UUID userId, UpdateProfileRequest request) {
        log.debug("Updating university profile for userId={}", userId);

        UniversityProfile profile = getProfileByUserId(userId);

        if (request.name() != null)
            profile.setName(request.name());
        if (request.bio() != null)
            profile.setBio(request.bio());
        if (request.websiteUrl() != null)
            profile.setWebsiteUrl(request.websiteUrl());
        if (request.address() != null)
            profile.setAddress(request.address());
        if (request.countryId() != null)
            profile.setCountryId(request.countryId());

        UniversityProfile saved = universityProfileRepository.save(profile);
        log.info("University profile updated — userId={}, profileId={}", userId, saved.getId());
        return mapper.toResponse(saved);
    }

    @Override
    public String uploadPhoto(UUID userId, MultipartFile file) {
        log.debug("Uploading photo for university userId={}", userId);

        UniversityProfile profile = getProfileByUserId(userId);

        String oldUrl = profile.getProfilePhotoUrl();
        if (oldUrl != null && !oldUrl.isBlank()) {
            log.debug("Deleting old photo for userId={}, url={}", userId, oldUrl);
            fileStorageService.delete(oldUrl);
        }

        String url = fileStorageService.upload(file, "universities/photos/" + userId);
        profile.setProfilePhotoUrl(url);
        universityProfileRepository.save(profile);
        log.info("University photo uploaded — userId={}, url={}", userId, url);
        return url;
    }

    private UniversityProfile getProfileByUserId(UUID userId) {
        return universityProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("University profile not found"));
    }
}