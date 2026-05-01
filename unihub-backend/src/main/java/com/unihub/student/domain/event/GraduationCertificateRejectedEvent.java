package com.unihub.student.domain.event;

import java.util.UUID;

public record GraduationCertificateRejectedEvent(
    UUID studentId, 
    String reason
) {

}
