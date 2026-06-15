package com.unihub.notifications.infrastructure.persistence.jpa;

import com.unihub.notifications.domain.enums.NotificationType;
import com.unihub.notifications.domain.model.NotificationPreferences;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JpaNotificationPreferencesRepository extends JpaRepository<NotificationPreferences, UUID> {

    Optional<NotificationPreferences> findByUserIdAndNotificationType(UUID userId, NotificationType type);

    List<NotificationPreferences> findAllByUserId(UUID userId);
}