package com.unihub.student.domain.event;

import java.util.UUID;

public record GraduationCertificateApprovedEvent(

        UUID profileId,
        UUID userId,
        UUID universityId
) {

}
