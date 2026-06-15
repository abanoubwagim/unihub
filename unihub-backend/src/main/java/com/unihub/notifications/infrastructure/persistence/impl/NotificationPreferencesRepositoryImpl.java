package com.unihub.notifications.infrastructure.persistence.impl;

import com.unihub.notifications.domain.enums.NotificationType;
import com.unihub.notifications.domain.model.NotificationPreferences;
import com.unihub.notifications.domain.repository.NotificationPreferencesRepository;
import com.unihub.notifications.infrastructure.persistence.jpa.JpaNotificationPreferencesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class NotificationPreferencesRepositoryImpl implements NotificationPreferencesRepository {

    private final JpaNotificationPreferencesRepository jpa;

    @Override
    public NotificationPreferences save(NotificationPreferences prefs) {
        return jpa.save(prefs);
    }

    @Override
    public void saveAll(List<NotificationPreferences> prefsList) {
        jpa.saveAll(prefsList);
    }

    @Override
    public Optional<NotificationPreferences> findByUserIdAndNotificationType(UUID userId, NotificationType type) {
        return jpa.findByUserIdAndNotificationType(userId, type);
    }

    @Override
    public List<NotificationPreferences> findAllByUserId(UUID userId) {
        return jpa.findAllByUserId(userId);
    }
}