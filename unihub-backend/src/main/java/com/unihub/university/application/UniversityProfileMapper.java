package com.unihub.university.application;

import com.unihub.university.api.dto.res.MajorResponse;
import com.unihub.university.api.dto.res.UniversityProfileResponse;
import com.unihub.university.domain.model.UniversityProfile;
import org.springframework.stereotype.Component;

@Component
public class UniversityProfileMapper {

    public UniversityProfileResponse toResponse(UniversityProfile profile) {
        return new UniversityProfileResponse(
                profile.getId(),
                profile.getUserId(),
                profile.getName(),
                profile.getBio(),
                profile.getProfilePhotoUrl(),
                profile.getWebsiteUrl(),
                profile.getAddress(),
                profile.getCountryId(),
                profile.getStudentCount(),
                profile.getGraduateCount(),
                profile.getMajors().stream()
                        .map(m -> new MajorResponse(m.getId(), m.getName()))
                        .toList()
        );
    }
}