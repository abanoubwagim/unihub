package com.unihub.student.api.dto;

import java.util.Set;
import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record UpdateSkillsRequest(
    
    @NotNull 
    Set<UUID> skillIds
) {

}
