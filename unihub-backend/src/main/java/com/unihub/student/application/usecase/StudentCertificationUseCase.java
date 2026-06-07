package com.unihub.student.application.usecase;

import com.unihub.shared.api.dto.PageResponse;
import com.unihub.student.api.dto.req.CertificationRequest;
import com.unihub.student.api.dto.res.CertificationResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface StudentCertificationUseCase {

    CertificationResponse add(UUID userId, CertificationRequest request, MultipartFile file);

    CertificationResponse update(UUID userId, UUID certId, CertificationRequest request, MultipartFile file);

    void delete(UUID userId, UUID certId);

    PageResponse<CertificationResponse> getAll(UUID userId, Pageable pageable);

    CertificationResponse getOne(UUID userId, UUID certId);

}
