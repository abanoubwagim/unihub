package com.unihub.company.application.mapper;

import com.unihub.company.api.dto.res.CompanyProfileResponse;
import com.unihub.company.domain.model.CompanyProfile;
import org.springframework.stereotype.Component;

@Component
public class CompanyProfileMapper {

    public CompanyProfileResponse toResponse(CompanyProfile profile) {
        return new CompanyProfileResponse(
                profile.getId(),
                profile.getUserId(),
                profile.getName(),
                profile.getDescription(),
                profile.getProfilePhotoUrl(),
                profile.getWebsiteUrl(),
                profile.getCountryId(),
                profile.getSpecialization(),
                profile.getHiredStudentCount()
        );
    }
}