package com.unihub.chat.application.impl;

import com.unihub.chat.api.dto.req.SendMessageRequest;
import com.unihub.chat.api.dto.res.ConversationSummaryResponse;
import com.unihub.chat.api.dto.res.MessageResponse;
import com.unihub.chat.application.usecase.ChatUseCase;
import com.unihub.chat.domain.event.ChatMessageSentEvent;
import com.unihub.chat.domain.model.Conversation;
import com.unihub.chat.domain.model.Message;
import com.unihub.chat.domain.repository.ConversationRepository;
import com.unihub.chat.domain.repository.MessageRepository;
import com.unihub.shared.api.dto.PageResponse;
import com.unihub.shared.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ChatUseCaseImpl implements ChatUseCase {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public MessageResponse sendMessage(UUID senderId, SendMessageRequest request) {

        if (senderId.equals(request.receiverId())) {
            throw new IllegalArgumentException("You cannot send messages to yourself");
        }

        Conversation conversation = conversationRepository
                .findByParticipants(senderId, request.receiverId())
                .orElseGet(() -> conversationRepository.save(
                        Conversation.builder()
                                .participant1Id(senderId)
                                .participant2Id(request.receiverId())
                                .build()));

        Message message = messageRepository.save(
                Message.builder()
                        .conversationId(conversation.getId())
                        .senderId(senderId)
                        .content(request.content())
                        .build());

        conversation.touch();

        MessageResponse response = toMessageResponse(message);

        // Push real-time notification to receiver
        messagingTemplate.convertAndSendToUser(
                request.receiverId().toString(),
                "/queue/messages",
                response);

        // Trigger in-app notification record via the notifications module
        eventPublisher.publishEvent(new ChatMessageSentEvent(
                conversation.getId(),
                senderId,
                request.receiverId()));

        log.info("Message sent — senderId={}, receiverId={}, conversationId={}",
                senderId, request.receiverId(), conversation.getId());

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<MessageResponse> getMessages(UUID userId, UUID conversationId, Pageable pageable) {
        conversationRepository.findById(conversationId)
                .filter(c -> c.getParticipant1Id().equals(userId) || c.getParticipant2Id().equals(userId))
                .orElseThrow(() -> new NotFoundException("Conversation not found"));

        Page<MessageResponse> page = messageRepository
                .findByConversationId(conversationId, pageable)
                .map(this::toMessageResponse);

        return PageResponse.from(page);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConversationSummaryResponse> getMyConversations(UUID userId) {
        return conversationRepository.findAllByUserId(userId)
                .stream()
                .map(c -> toConversationSummary(c, userId))
                .toList();
    }

    @Override
    public void markAsRead(UUID userId, UUID conversationId) {
        conversationRepository.findById(conversationId)
                .filter(c -> c.getParticipant1Id().equals(userId) || c.getParticipant2Id().equals(userId))
                .orElseThrow(() -> new NotFoundException("Conversation not found"));

        messageRepository.markConversationAsRead(conversationId, userId);
        log.debug("Messages marked as read — userId={}, conversationId={}", userId, conversationId);
    }

    private MessageResponse toMessageResponse(Message message) {
        return new MessageResponse(
                message.getId(),
                message.getConversationId(),
                message.getSenderId(),
                message.getContent(),
                message.getStatus(),
                message.getCreatedAt());
    }

    private ConversationSummaryResponse toConversationSummary(Conversation c, UUID currentUserId) {
        UUID otherParticipantId = c.getParticipant1Id().equals(currentUserId)
                ? c.getParticipant2Id()
                : c.getParticipant1Id();
        return new ConversationSummaryResponse(c.getId(), otherParticipantId, c.getUpdatedAt());
    }
}