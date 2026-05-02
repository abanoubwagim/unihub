package com.unihub.student.application.usecase;

import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import com.unihub.shared.dto.PageResponse;
import com.unihub.student.api.dto.CertificationRequest;
import com.unihub.student.api.dto.CertificationResponse;

public interface StudentCertificationUseCase {

    CertificationResponse add(UUID userId, CertificationRequest request, MultipartFile file);

    CertificationResponse update(UUID userId, UUID certId, CertificationRequest request, MultipartFile file);

    void delete(UUID userId, UUID certId);

    PageResponse<CertificationResponse> getAll(UUID userId, Pageable pageable);

    CertificationResponse getOne(UUID userId, UUID certId);

}
