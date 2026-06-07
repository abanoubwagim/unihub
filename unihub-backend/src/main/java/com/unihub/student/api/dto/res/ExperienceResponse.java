package com.unihub.student.api.dto.res;

import com.unihub.shared.domain.enums.JobType;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record ExperienceResponse(

        UUID id,
        String jobTitle,
        String company,
        JobType jobType,
        LocalDate startDate,
        LocalDate endDate,
        boolean current,
        String location,
        String description,
        List<String> skills
) {
}