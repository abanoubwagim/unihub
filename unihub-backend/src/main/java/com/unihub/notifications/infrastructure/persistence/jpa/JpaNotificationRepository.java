package com.unihub.notifications.infrastructure.persistence.jpa;

import com.unihub.notifications.domain.model.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface JpaNotificationRepository extends JpaRepository<Notification, UUID> {

    Optional<Notification> findByIdAndUserId(UUID id, UUID userId);

    Page<Notification> findAllByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    long countByUserIdAndReadFalse(UUID userId);

    @Modifying
    @Query("""
            UPDATE Notification n
            SET    n.read   = true,
                   n.readAt = :now
            WHERE  n.userId = :userId
              AND  n.read   = false
            """)
    void markAllReadByUserId(@Param("userId") UUID userId,
                             @Param("now") LocalDateTime now);

    void deleteByIdAndUserId(UUID id, UUID userId);
}