package com.unihub.company.application.impl;

import com.unihub.company.domain.model.CompanyProfile;
import com.unihub.company.domain.repository.CompanyProfileRepository;
import com.unihub.shared.api.external.CompanyUserIdApi;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CompanyUserIdApiImpl implements CompanyUserIdApi {

    private final CompanyProfileRepository companyProfileRepository;

    @Override
    @Transactional(readOnly = true)
    public Optional<UUID> findUserIdByCompanyProfileId(UUID companyProfileId) {
        return companyProfileRepository.findById(companyProfileId)
                .map(CompanyProfile::getUserId);
    }
}