package com.unihub.student.application.impl;

import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.unihub.shared.dto.PageResponse;
import com.unihub.shared.exception.NotFoundException;
import com.unihub.shared.storage.FileStorageService;
import com.unihub.student.api.dto.CertificationRequest;
import com.unihub.student.api.dto.CertificationResponse;
import com.unihub.student.application.usecase.StudentCertificationUseCase;
import com.unihub.student.domain.model.StudentCertification;
import com.unihub.student.domain.repository.StudentCertificationRepository;
import com.unihub.student.domain.repository.StudentProfileRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class StudentCertificationUseCaseImpl implements StudentCertificationUseCase {

    private final StudentProfileRepository profileRepository;
    private final StudentCertificationRepository certificationRepository;
    private final FileStorageService fileStorageService;

    public CertificationResponse add(UUID userId, CertificationRequest request, MultipartFile file) {
        var profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("Student profile not found"));

        String fileUrl = null;
        if (file != null && !file.isEmpty()) {
            fileUrl = fileStorageService.upload(file, "students/certifications/" + userId);
        }

        var cert = new StudentCertification();
        cert.setStudent(profile);
        cert.setTitle(request.title());
        cert.setIssuingOrganization(request.issuingOrganization());
        cert.setDateIssued(request.dateIssued());
        cert.setFileUrl(fileUrl);

        return toResponse(certificationRepository.save(cert));
    }

    public CertificationResponse update(UUID userId, UUID certId, CertificationRequest request, MultipartFile file) {
        var cert = getOwnedCertification(userId, certId);

        cert.setTitle(request.title());
        cert.setIssuingOrganization(request.issuingOrganization());
        cert.setDateIssued(request.dateIssued());

        if (file != null && !file.isEmpty()) {
            if (cert.getFileUrl() != null) {
                fileStorageService.delete(cert.getFileUrl());
            }
            cert.setFileUrl(fileStorageService.upload(file, "students/certifications/" + userId));
        }

        return toResponse(certificationRepository.save(cert));
    }

    @Transactional(readOnly = true)
    public PageResponse<CertificationResponse> getAll(UUID userId, Pageable pageable) {
        return PageResponse.from(
                certificationRepository.findAllByStudent_UserId(userId, pageable)
                        .map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public CertificationResponse getOne(UUID userId, UUID certId) {
        return certificationRepository.findByIdAndStudent_UserId(certId, userId)
                .map(this::toResponse)
                .orElseThrow(() -> new NotFoundException("Certification not found"));
    }

    public void delete(UUID userId, UUID certId) {
        var cert = getOwnedCertification(userId, certId);
        if (cert.getFileUrl() != null) {
            fileStorageService.delete(cert.getFileUrl());
        }
        certificationRepository.delete(cert);
    }

    private StudentCertification getOwnedCertification(UUID userId, UUID certId) {
        var cert = certificationRepository.findById(certId)
                .orElseThrow(() -> new NotFoundException("Certification not found"));
        if (!cert.getStudent().getUserId().equals(userId)) {
            throw new NotFoundException("Certification not found");
        }
        return cert;
    }

    private CertificationResponse toResponse(StudentCertification cert) {
        return new CertificationResponse(
                cert.getId(),
                cert.getTitle(),
                cert.getIssuingOrganization(),
                cert.getDateIssued(),
                cert.getFileUrl());
    }
}
