package com.unihub.company.application.impl;

import com.unihub.company.api.dto.req.UpdateProfileRequest;
import com.unihub.company.api.dto.res.CompanyProfileResponse;
import com.unihub.company.application.mapper.CompanyProfileMapper;
import com.unihub.company.application.usecase.CompanyProfileUseCase;
import com.unihub.company.domain.model.CompanyProfile;
import com.unihub.company.domain.repository.CompanyProfileRepository;
import com.unihub.shared.exception.NotFoundException;
import com.unihub.shared.storage.FileStorageService;
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
public class CompanyProfileUseCaseImpl implements CompanyProfileUseCase {

    private final CompanyProfileRepository companyProfileRepository;
    private final FileStorageService fileStorageService;
    private final CompanyProfileMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public CompanyProfileResponse getMyProfile(UUID userId) {
        return mapper.toResponse(getProfileByUserId(userId));
    }

    @Override
    public CompanyProfileResponse updateProfile(UUID userId, UpdateProfileRequest request) {
        log.debug("Updating company profile for userId={}", userId);

        CompanyProfile profile = getProfileByUserId(userId);

        if (request.name() != null) profile.setName(request.name());
        if (request.description() != null) profile.setDescription(request.description());
        if (request.websiteUrl() != null) profile.setWebsiteUrl(request.websiteUrl());
        if (request.countryId() != null) profile.setCountryId(request.countryId());
        if (request.specialization() != null) profile.setSpecialization(request.specialization());

        CompanyProfile saved = companyProfileRepository.save(profile);
        log.info("Company profile updated — userId={}, profileId={}", userId, saved.getId());
        return mapper.toResponse(saved);
    }

    @Override
    public String uploadPhoto(UUID userId, MultipartFile file) {
        log.debug("Uploading photo for company userId={}", userId);

        CompanyProfile profile = getProfileByUserId(userId);

        String oldUrl = profile.getProfilePhotoUrl();
        if (oldUrl != null && !oldUrl.isBlank()) {
            log.debug("Deleting old photo for userId={}, url={}", userId, oldUrl);
            fileStorageService.delete(oldUrl);
        }

        String url = fileStorageService.upload(file, "companies/photos/" + userId);
        profile.setProfilePhotoUrl(url);
        companyProfileRepository.save(profile);
        log.info("Company photo uploaded — userId={}, url={}", userId, url);
        return url;
    }

    private CompanyProfile getProfileByUserId(UUID userId) {
        return companyProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("Company profile not found"));
    }
}