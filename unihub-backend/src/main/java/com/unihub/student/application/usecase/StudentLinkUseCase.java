package com.unihub.student.application.usecase;

import com.unihub.student.api.dto.req.LinkRequest;
import com.unihub.student.api.dto.res.LinkResponse;

import java.util.List;
import java.util.UUID;

public interface StudentLinkUseCase {

    LinkResponse add(UUID userId, LinkRequest request);

    LinkResponse update(UUID userId, UUID linkId, LinkRequest request);

    void delete(UUID userId, UUID linkId);

    List<LinkResponse> getAll(UUID userId);

    LinkResponse getOne(UUID userId, UUID linkId);
}