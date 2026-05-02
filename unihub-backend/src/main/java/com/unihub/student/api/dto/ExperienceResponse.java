package com.unihub.student.api.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.unihub.shared.enums.JobType;




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
) {}