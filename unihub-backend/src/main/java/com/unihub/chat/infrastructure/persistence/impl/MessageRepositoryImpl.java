package com.unihub.chat.infrastructure.persistence.impl;

import com.unihub.chat.domain.enums.MessageStatus;
import com.unihub.chat.domain.model.Message;
import com.unihub.chat.domain.repository.MessageRepository;
import com.unihub.chat.infrastructure.persistence.jpa.JpaMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class MessageRepositoryImpl implements MessageRepository {

    private final JpaMessageRepository jpa;

    @Override
    public Message save(Message message) {
        return jpa.save(message);
    }

    @Override
    public Page<Message> findByConversationId(UUID conversationId, Pageable pageable) {
        return jpa.findByConversationIdOrderByCreatedAtDesc(conversationId, pageable);
    }

    @Override
    public void markConversationAsRead(UUID conversationId, UUID readerId) {
        jpa.markAsRead(conversationId, readerId, MessageStatus.READ);
    }
}
