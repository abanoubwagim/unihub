package com.unihub.university.application.usecase;

import com.unihub.university.api.dto.res.MajorResponse;

import java.util.List;
import java.util.UUID;

public interface UniversityMajorUseCase {

    List<MajorResponse> getAllAvailable();

    List<MajorResponse> getSelected(UUID userId);

    void select(UUID userId, UUID majorId);

}