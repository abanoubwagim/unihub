package com.unihub.chat.domain.repository;

import com.unihub.chat.domain.model.Conversation;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ConversationRepository {

    Conversation save(Conversation conversation);

    Optional<Conversation> findById(UUID id);

    Optional<Conversation> findByParticipants(UUID userId1, UUID userId2);

    List<Conversation> findAllByUserId(UUID userId);
}
