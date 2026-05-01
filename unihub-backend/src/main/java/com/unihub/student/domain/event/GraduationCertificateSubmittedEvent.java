package com.unihub.student.domain.event;

import java.util.UUID;

public record GraduationCertificateSubmittedEvent(
    UUID studentId,
    UUID universityId,
    String fileUrl
) {}
