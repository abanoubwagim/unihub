package com.unihub.chat.infrastructure.persistence.impl;

import com.unihub.chat.domain.model.Conversation;
import com.unihub.chat.domain.repository.ConversationRepository;
import com.unihub.chat.infrastructure.persistence.jpa.JpaConversationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class ConversationRepositoryImpl implements ConversationRepository {

    private final JpaConversationRepository jpa;

    @Override
    public Conversation save(Conversation conversation) {
        return jpa.save(conversation);
    }

    @Override
    public Optional<Conversation> findById(UUID id) {
        return jpa.findById(id);
    }

    @Override
    public Optional<Conversation> findByParticipants(UUID userId1, UUID userId2) {
        return jpa.findByParticipants(userId1, userId2);
    }

    @Override
    public List<Conversation> findAllByUserId(UUID userId) {
        return jpa.findAllByUserId(userId);
    }
}
