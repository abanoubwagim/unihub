package com.unihub.student.application.usecase;

import com.unihub.shared.api.dto.PageResponse;
import com.unihub.student.api.dto.req.ProjectRequest;
import com.unihub.student.api.dto.res.ProjectResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface StudentProjectUseCase {

    ProjectResponse add(UUID userId, ProjectRequest request);

    ProjectResponse update(UUID userId, UUID projectId, ProjectRequest request);

    void delete(UUID userId, UUID projectId);

    PageResponse<ProjectResponse> getAll(UUID userId, Pageable pageable);

    ProjectResponse getOne(UUID userId, UUID projectId);
}
