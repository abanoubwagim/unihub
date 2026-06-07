package com.unihub.university.application.usecase;

import com.unihub.shared.api.dto.PageResponse;
import com.unihub.university.api.dto.res.UniversityPublicResponse;
import org.springframework.data.domain.Pageable;

public interface UniversityPublicUseCase {

    PageResponse<UniversityPublicResponse> getAll(Pageable pageable);
}