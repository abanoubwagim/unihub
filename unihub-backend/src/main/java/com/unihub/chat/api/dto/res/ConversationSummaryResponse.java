package com.unihub.chat.api.dto.res;

import java.time.LocalDateTime;
import java.util.UUID;

public record ConversationSummaryResponse(
        UUID id,
        UUID otherParticipantId,
        LocalDateTime updatedAt
) {
}
