package com.unihub.chat.infrastructure.persistence.jpa;

import com.unihub.chat.domain.model.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JpaConversationRepository extends JpaRepository<Conversation, UUID> {

    @Query("""
            SELECT c FROM Conversation c
            WHERE (c.participant1Id = :a AND c.participant2Id = :b)
               OR (c.participant1Id = :b AND c.participant2Id = :a)
            """)
    Optional<Conversation> findByParticipants(@Param("a") UUID a, @Param("b") UUID b);

    @Query("""
            SELECT c FROM Conversation c
            WHERE c.participant1Id = :userId OR c.participant2Id = :userId
            ORDER BY c.updatedAt DESC
            """)
    List<Conversation> findAllByUserId(@Param("userId") UUID userId);
}
