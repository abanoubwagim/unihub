package com.unihub.student.api.dto.res;

import java.time.LocalDate;
import java.util.UUID;

public record CertificationResponse(

        UUID id,
        String title,
        String issuingOrganization,
        LocalDate dateIssued,
        String fileUrl
) {
}