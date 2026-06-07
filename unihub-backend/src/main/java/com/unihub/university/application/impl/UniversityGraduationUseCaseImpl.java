package com.unihub.university.application.impl;

import com.unihub.shared.api.dto.PageResponse;
import com.unihub.shared.api.dto.external.PendingCertInfo;
import com.unihub.shared.api.external.StudentGraduationApi;
import com.unihub.shared.exception.NotFoundException;
import com.unihub.university.api.dto.req.ReviewCertificateRequest;
import com.unihub.university.api.dto.res.PendingCertSummaryResponse;
import com.unihub.university.application.usecase.UniversityGraduationUseCase;
import com.unihub.university.domain.model.UniversityProfile;
import com.unihub.university.domain.repository.UniversityProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UniversityGraduationUseCaseImpl implements UniversityGraduationUseCase {

    private final UniversityProfileRepository universityProfileRepository;
    private final StudentGraduationApi studentGraduationApi;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PendingCertSummaryResponse> getPendingCertificates(UUID userId, Pageable pageable) {
        UniversityProfile profile = getProfileByUserId(userId);
        Page<PendingCertInfo> page = studentGraduationApi.getPendingCertificates(profile.getId(), pageable);
        return PageResponse.from(page.map(c -> new PendingCertSummaryResponse(
                c.certId(),
                c.studentProfileId(),
                c.studentName(),
                c.studentPhotoUrl(),
                c.fileUrl(),
                c.attemptNumber(),
                c.submittedAt())
        ));
    }

    @Override
    public void reviewCertificate(UUID userId, UUID certId, ReviewCertificateRequest request) {
        log.debug("University reviewing certificate — userId={}, certId={}, approved={}", userId, certId, request.approved());

        UniversityProfile profile = getProfileByUserId(userId);
        studentGraduationApi.reviewCertificate(certId, profile.getId(), request.approved(), request.rejectionReason());
        log.info("Certificate reviewed — userId={}, certId={}, approved={}", userId, certId, request.approved());
    }

    private UniversityProfile getProfileByUserId(UUID userId) {
        return universityProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("University profile not found"));
    }
}