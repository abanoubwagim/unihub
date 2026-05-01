package com.unihub.student.domain.event;

import java.util.UUID;

public record StudentProfileUpdatedEvent(
   
    UUID profileId, 
    UUID userId

) { }
