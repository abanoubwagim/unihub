package com.unihub.student.api.dto.res;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record ProjectResponse(

        UUID id,
        String title,
        String description,
        LocalDate startDate,
        LocalDate endDate,
        Boolean current,
        String projectLink,
        List<String> skills
) {
}