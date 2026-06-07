package com.unihub.university.application.usecase;

import com.unihub.university.api.dto.req.UpdateProfileRequest;
import com.unihub.university.api.dto.res.UniversityProfileResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface UniversityProfileUseCase {

    UniversityProfileResponse getMyProfile(UUID userId);

    UniversityProfileResponse updateProfile(UUID userId, UpdateProfileRequest request);

    String uploadPhoto(UUID userId, MultipartFile file);
}