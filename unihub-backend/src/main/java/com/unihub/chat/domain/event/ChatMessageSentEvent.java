package com.unihub.chat.domain.event;

import java.util.UUID;

public record ChatMessageSentEvent(
        UUID conversationId,
        UUID senderId,
        UUID receiverId
) {
}