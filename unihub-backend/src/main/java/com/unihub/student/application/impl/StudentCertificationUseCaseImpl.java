package com.unihub.student.application.impl;

import com.unihub.shared.api.dto.PageResponse;
import com.unihub.shared.exception.NotFoundException;
import com.unihub.shared.storage.FileStorageService;
import com.unihub.student.api.dto.req.CertificationRequest;
import com.unihub.student.api.dto.res.CertificationResponse;
import com.unihub.student.application.usecase.StudentCertificationUseCase;
import com.unihub.student.domain.model.StudentCertification;
import com.unihub.student.domain.model.StudentProfile;
import com.unihub.student.domain.repository.StudentCertificationRepository;
import com.unihub.student.domain.repository.StudentProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class StudentCertificationUseCaseImpl implements StudentCertificationUseCase {

    private final StudentProfileRepository profileRepository;
    private final StudentCertificationRepository certificationRepository;
    private final FileStorageService fileStorageService;

    @Override
    public CertificationResponse add(UUID userId, CertificationRequest request, MultipartFile file) {
        log.debug("Adding certification for userId={}, title={}", userId, request.title());

        StudentProfile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("Student profile not found"));

        String fileUrl = null;
        if (file != null && !file.isEmpty()) {
            log.debug("Uploading certification file for userId={}", userId);
            fileUrl = fileStorageService.upload(file, "students/certifications/" + userId);
        }

        StudentCertification cert = StudentCertification.builder()
                .student(profile)
                .title(request.title())
                .issuingOrganization(request.issuingOrganization())
                .dateIssued(request.dateIssued())
                .fileUrl(fileUrl)
                .build();
        CertificationResponse response = toResponse(certificationRepository.save(cert));
        log.info("Certification added — userId={}, certId={}", userId, response.id());
        return response;
    }

    @Override
    public CertificationResponse update(UUID userId, UUID certId, CertificationRequest request, MultipartFile file) {
        log.debug("Updating certification — userId={}, certId={}", userId, certId);

        StudentCertification cert = getOwnedCertification(userId, certId);

        cert.setTitle(request.title());
        cert.setIssuingOrganization(request.issuingOrganization());
        cert.setDateIssued(request.dateIssued());

        if (file != null && !file.isEmpty()) {
            String oldUrl = cert.getFileUrl();
            log.debug("Replacing cert file — userId={}, certId={}, oldUrl={}", userId, certId, oldUrl);

            String newUrl = fileStorageService.upload(file, "students/certifications/" + userId);
            cert.setFileUrl(newUrl);
            certificationRepository.save(cert);

            if (oldUrl != null && !oldUrl.isBlank()) {
                fileStorageService.delete(oldUrl);
                log.debug("Old cert file deleted — url={}", oldUrl);
            }

            log.info("Certification updated with new file — userId={}, certId={}, newUrl={}", userId, certId, newUrl);
            return toResponse(cert);
        }

        log.info("Certification updated without file change — userId={}, certId={}", userId, certId);
        return toResponse(certificationRepository.save(cert));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CertificationResponse> getAll(UUID userId, Pageable pageable) {
        return PageResponse.from(
                certificationRepository.findAllByStudent_UserId(userId, pageable)
                        .map(this::toResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public CertificationResponse getOne(UUID userId, UUID certId) {
        return certificationRepository.findByIdAndStudent_UserId(certId, userId)
                .map(this::toResponse)
                .orElseThrow(() -> new NotFoundException("Certification not found"));
    }

    @Override
    public void delete(UUID userId, UUID certId) {
        log.debug("Deleting certification — userId={}, certId={}", userId, certId);

        StudentCertification cert = getOwnedCertification(userId, certId);
        String fileUrl = cert.getFileUrl();
        certificationRepository.delete(cert);
        log.info("Certification deleted from DB — userId={}, certId={}", userId, certId);

        if (fileUrl != null) {
            fileStorageService.delete(fileUrl);
            log.info("Certification file deleted — userId={}, certId={}", userId, certId);
        }
    }

    private StudentCertification getOwnedCertification(UUID userId, UUID certId) {
        return certificationRepository.findByIdAndStudent_UserId(certId, userId)
                .orElseThrow(() -> {
                    log.warn("Unauthorized or missing certification access — userId={}, certId={}", userId, certId);
                    return new NotFoundException("Certification not found");
                });
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
