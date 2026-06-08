package com.unihub.company.application.impl;

import com.unihub.company.application.usecase.CompanyProfileCreator;
import com.unihub.company.domain.model.CompanyProfile;
import com.unihub.company.domain.repository.CompanyProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CompanyProfileCreatorImpl implements CompanyProfileCreator {

    private final CompanyProfileRepository companyProfileRepository;

    @Override
    public void createEmptyProfile(UUID userId) {
        if (companyProfileRepository.existsByUserId(userId)) {
            log.debug("Company profile already exists for userId={}, skipping", userId);
            return;
        }
        try {
            CompanyProfile profile = new CompanyProfile();
            profile.setUserId(userId);
            companyProfileRepository.save(profile);
            log.info("Created empty company profile for userId={}", userId);
        } catch (DataIntegrityViolationException e) {
            log.warn("Concurrent company profile creation for userId={} — already exists", userId);
        }
    }
}