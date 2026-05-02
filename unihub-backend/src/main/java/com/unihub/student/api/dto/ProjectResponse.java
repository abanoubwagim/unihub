package com.unihub.student.api.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record ProjectResponse(
    
    UUID id,
    String title,
    String description,
    LocalDate startDate,
    LocalDate endDate,
    String projectLink,
    List<String> skills
) {}