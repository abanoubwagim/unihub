package com.unihub.notifications.domain.repository;

import com.unihub.notifications.domain.enums.NotificationType;
import com.unihub.notifications.domain.model.NotificationPreferences;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NotificationPreferencesRepository {

    NotificationPreferences save(NotificationPreferences prefs);

    void saveAll(List<NotificationPreferences> prefsList);

    Optional<NotificationPreferences> findByUserIdAndNotificationType(UUID userId, NotificationType type);

    List<NotificationPreferences> findAllByUserId(UUID userId);
}