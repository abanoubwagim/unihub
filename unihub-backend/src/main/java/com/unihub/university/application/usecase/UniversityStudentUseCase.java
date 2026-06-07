package com.unihub.university.application.usecase;

import com.unihub.shared.api.dto.PageResponse;
import com.unihub.university.api.dto.res.UniversityStudentSummaryResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface UniversityStudentUseCase {

    PageResponse<UniversityStudentSummaryResponse> getMyStudents(UUID userId, Pageable pageable);

    UniversityStudentSummaryResponse getStudent(UUID userId, UUID studentProfileId);
}