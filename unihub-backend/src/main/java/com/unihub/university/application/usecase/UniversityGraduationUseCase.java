package com.unihub.university.application.usecase;

import com.unihub.shared.api.dto.PageResponse;
import com.unihub.university.api.dto.req.ReviewCertificateRequest;
import com.unihub.university.api.dto.res.PendingCertSummaryResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface UniversityGraduationUseCase {

    PageResponse<PendingCertSummaryResponse> getPendingCertificates(UUID userId, Pageable pageable);

    void reviewCertificate(UUID userId, UUID certId, ReviewCertificateRequest request);
}