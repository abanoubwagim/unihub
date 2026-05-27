package com.unihub.student.application.usecase;

import java.util.UUID;

import org.springframework.data.domain.Pageable;

import com.unihub.shared.dto.PageResponse;
import com.unihub.student.api.dto.req.ProjectRequest;
import com.unihub.student.api.dto.res.ProjectResponse;

public interface StudentProjectUseCase {

    ProjectResponse add(UUID userId, ProjectRequest request);

    ProjectResponse update(UUID userId, UUID projectId, ProjectRequest request);

    void delete(UUID userId, UUID projectId);

    PageResponse<ProjectResponse> getAll(UUID userId, Pageable pageable);

    ProjectResponse getOne(UUID userId, UUID projectId);
}
