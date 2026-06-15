package com.unihub.notifications.domain.repository;

import com.unihub.notifications.domain.model.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface NotificationRepository {

    Notification save(Notification notification);

    Optional<Notification> findByIdAndUserId(UUID id, UUID userId);

    Page<Notification> findAllByUserId(UUID userId, Pageable pageable);

    long countByUserIdAndReadFalse(UUID userId);
    
    void markAllReadByUserId(UUID userId);

    void deleteByIdAndUserId(UUID id, UUID userId);
}