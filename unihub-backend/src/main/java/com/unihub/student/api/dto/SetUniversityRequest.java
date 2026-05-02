package com.unihub.student.api.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record SetUniversityRequest(

    @NotNull 
    UUID universityId,
    @NotNull 
    UUID majorId
) {

}
