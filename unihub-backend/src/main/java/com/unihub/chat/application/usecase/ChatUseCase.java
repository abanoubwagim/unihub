package com.unihub.chat.application.usecase;

import com.unihub.chat.api.dto.req.SendMessageRequest;
import com.unihub.chat.api.dto.res.ConversationSummaryResponse;
import com.unihub.chat.api.dto.res.MessageResponse;
import com.unihub.shared.api.dto.PageResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface ChatUseCase {

    MessageResponse sendMessage(UUID senderId, SendMessageRequest request);

    PageResponse<MessageResponse> getMessages(UUID userId, UUID conversationId, Pageable pageable);

    List<ConversationSummaryResponse> getMyConversations(UUID userId);

    void markAsRead(UUID userId, UUID conversationId);
}
