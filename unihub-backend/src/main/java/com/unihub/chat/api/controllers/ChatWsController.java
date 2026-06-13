package com.unihub.chat.api.controllers;

import com.unihub.chat.api.dto.req.SendMessageRequest;
import com.unihub.chat.api.dto.res.MessageResponse;
import com.unihub.chat.application.usecase.ChatUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class ChatWsController {

    private final ChatUseCase chatUseCase;

    @MessageMapping("/chat.send")
    @SendToUser("/queue/messages")
    public MessageResponse send(
            @Payload @Valid SendMessageRequest request,
            Authentication authentication) {
        UUID senderId = UUID.fromString(authentication.getName());
        return chatUseCase.sendMessage(senderId, request);
    }
}
