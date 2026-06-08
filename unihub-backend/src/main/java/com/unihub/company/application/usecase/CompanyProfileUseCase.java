package com.unihub.company.application.usecase;

import com.unihub.company.api.dto.req.UpdateProfileRequest;
import com.unihub.company.api.dto.res.CompanyProfileResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface CompanyProfileUseCase {

    CompanyProfileResponse getMyProfile(UUID userId);

    CompanyProfileResponse updateProfile(UUID userId, UpdateProfileRequest request);

    String uploadPhoto(UUID userId, MultipartFile file);
}