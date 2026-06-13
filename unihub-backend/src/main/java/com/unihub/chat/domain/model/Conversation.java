package com.unihub.chat.domain.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "conversations",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_conversation_participants",
                columnNames = {"participant1_id", "participant2_id"}
        )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Conversation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "participant1_id", nullable = false)
    private UUID participant1Id;

    @Column(name = "participant2_id", nullable = false)
    private UUID participant2Id;

    @CreationTimestamp
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public void touch() {
        this.updatedAt = LocalDateTime.now();
    }
}
