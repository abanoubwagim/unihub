package com.unihub.notifications.infrastructure.persistence.impl;

import com.unihub.notifications.domain.model.Notification;
import com.unihub.notifications.domain.repository.NotificationRepository;
import com.unihub.notifications.infrastructure.persistence.jpa.JpaNotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class NotificationRepositoryImpl implements NotificationRepository {

    private final JpaNotificationRepository jpa;

    @Override
    public Notification save(Notification notification) {
        return jpa.save(notification);
    }

    @Override
    public Optional<Notification> findByIdAndUserId(UUID id, UUID userId) {
        return jpa.findByIdAndUserId(id, userId);
    }

    @Override
    public Page<Notification> findAllByUserId(UUID userId, Pageable pageable) {
        return jpa.findAllByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    @Override
    public long countByUserIdAndReadFalse(UUID userId) {
        return jpa.countByUserIdAndReadFalse(userId);
    }

    @Override
    public void markAllReadByUserId(UUID userId) {
        jpa.markAllReadByUserId(userId, LocalDateTime.now());
    }

    @Override
    public void deleteByIdAndUserId(UUID id, UUID userId) {
        jpa.deleteByIdAndUserId(id, userId);
    }
}