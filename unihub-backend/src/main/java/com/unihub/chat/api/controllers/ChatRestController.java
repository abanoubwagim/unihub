package com.unihub.chat.api.controllers;

import com.unihub.chat.api.dto.res.ConversationSummaryResponse;
import com.unihub.chat.api.dto.res.MessageResponse;
import com.unihub.chat.application.usecase.ChatUseCase;
import com.unihub.shared.api.dto.PageResponse;
import com.unihub.shared.security.JwtSubject;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
public class ChatRestController {

    private final ChatUseCase chatUseCase;

    @GetMapping("/conversations")
    public ResponseEntity<List<ConversationSummaryResponse>> getMyConversations(
            Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        return ResponseEntity.ok(chatUseCase.getMyConversations(userId));
    }

    @GetMapping("/conversations/{conversationId}/messages")
    public ResponseEntity<PageResponse<MessageResponse>> getMessages(
            Authentication authentication,
            @PathVariable UUID conversationId,
            @PageableDefault(size = 20) Pageable pageable) {
        UUID userId = UUID.fromString(authentication.getName());
        return ResponseEntity.ok(chatUseCase.getMessages(userId, conversationId, pageable));
    }

    @PatchMapping("/conversations/{conversationId}/read")
    public ResponseEntity<Void> markAsRead(
            @AuthenticationPrincipal JwtSubject principal,
            @PathVariable UUID conversationId) {
        chatUseCase.markAsRead(principal.id(), conversationId);
        return ResponseEntity.noContent().build();
    }
}