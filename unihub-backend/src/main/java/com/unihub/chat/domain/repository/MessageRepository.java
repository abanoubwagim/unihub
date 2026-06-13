package com.unihub.chat.domain.repository;

import com.unihub.chat.domain.model.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface MessageRepository {

    Message save(Message message);

    Page<Message> findByConversationId(UUID conversationId, Pageable pageable);

    void markConversationAsRead(UUID conversationId, UUID readerId);
}
