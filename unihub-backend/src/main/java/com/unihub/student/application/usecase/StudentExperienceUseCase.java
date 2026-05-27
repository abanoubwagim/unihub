package com.unihub.student.application.usecase;

import java.util.UUID;

import org.springframework.data.domain.Pageable;

import com.unihub.shared.dto.PageResponse;
import com.unihub.student.api.dto.req.ExperienceRequest;
import com.unihub.student.api.dto.res.ExperienceResponse;

public interface StudentExperienceUseCase {

    ExperienceResponse add(UUID userId, ExperienceRequest request);

    ExperienceResponse update(UUID userId, UUID experienceId, ExperienceRequest request);

    void delete(UUID userId, UUID experienceId);

    PageResponse<ExperienceResponse> getAll(UUID userId, Pageable pageable);

    ExperienceResponse getOne(UUID userId, UUID experienceId);
}
