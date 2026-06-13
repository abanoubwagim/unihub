package com.unihub.chat.api.dto.res;

import com.unihub.chat.domain.enums.MessageStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record MessageResponse(
        UUID id,
        UUID conversationId,
        UUID senderId,
        String content,
        MessageStatus status,
        LocalDateTime createdAt
) {
}
