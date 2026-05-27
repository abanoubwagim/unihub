package com.unihub.student.application;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface StudentGraduationApi {

    Page<PendingCertInfo> getPendingCertificates(UUID universityProfileId, Pageable pageable);

    void reviewCertificate(UUID certId, UUID reviewerUniversityId, boolean approved, String rejectionReason);
}