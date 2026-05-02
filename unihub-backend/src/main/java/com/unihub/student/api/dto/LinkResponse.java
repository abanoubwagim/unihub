package com.unihub.student.api.dto;

import com.unihub.student.domain.enums.LinkType;

public record LinkResponse(
    
    LinkType type,
    String label,
    String url
) {}