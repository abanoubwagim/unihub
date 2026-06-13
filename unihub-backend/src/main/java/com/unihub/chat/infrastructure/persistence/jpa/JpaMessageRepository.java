package com.unihub.chat.infrastructure.persistence.jpa;

import com.unihub.chat.domain.enums.MessageStatus;
import com.unihub.chat.domain.model.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface JpaMessageRepository extends JpaRepository<Message, UUID> {

    Page<Message> findByConversationIdOrderByCreatedAtDesc(UUID conversationId, Pageable pageable);

    @Modifying
    @Query("""
            UPDATE Message m
            SET m.status = :status
            WHERE m.conversationId = :convId
              AND m.senderId != :readerId
              AND m.status = 'SENT'
            """)
    void markAsRead(
            @Param("convId") UUID convId,
            @Param("readerId") UUID readerId,
            @Param("status") MessageStatus status);
}
