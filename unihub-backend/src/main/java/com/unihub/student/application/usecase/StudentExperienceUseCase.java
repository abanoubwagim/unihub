package com.unihub.student.application.usecase;

import com.unihub.shared.api.dto.PageResponse;
import com.unihub.student.api.dto.req.ExperienceRequest;
import com.unihub.student.api.dto.res.ExperienceResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface StudentExperienceUseCase {

    ExperienceResponse add(UUID userId, ExperienceRequest request);

    ExperienceResponse update(UUID userId, UUID experienceId, ExperienceRequest request);

    void delete(UUID userId, UUID experienceId);

    PageResponse<ExperienceResponse> getAll(UUID userId, Pageable pageable);

    ExperienceResponse getOne(UUID userId, UUID experienceId);
}
