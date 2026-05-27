package com.unihub.student.api.dto.res;

import java.util.UUID;

import com.unihub.student.domain.enums.GraduationCertificateStatus;

public record GraduationCertResponse(
        UUID id,
        GraduationCertificateStatus status,
        int attemptNumber,
        String rejectionReason
) {
}