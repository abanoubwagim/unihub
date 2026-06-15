package com.unihub.notifications.application.service;

import com.unihub.notifications.api.dto.res.NotificationResponse;
import com.unihub.notifications.domain.enums.NotificationType;
import com.unihub.notifications.domain.model.Notification;
import com.unihub.notifications.domain.model.NotificationPreferences;
import com.unihub.notifications.domain.repository.NotificationPreferencesRepository;
import com.unihub.notifications.domain.repository.NotificationRepository;
import com.unihub.notifications.infrastructure.channel.NotificationSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationDispatcher {

    private static final boolean DEFAULT_IN_APP_ENABLED = true;
    private final NotificationRepository notificationRepository;
    private final NotificationPreferencesRepository preferencesRepository;
    private final List<NotificationSender> senders;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void dispatch(UUID recipientUserId,
                         NotificationType type,
                         String title,
                         String body,
                         UUID referenceId,
                         String referenceType) {

        if (recipientUserId == null) {
            log.warn("NotificationDispatcher: recipientUserId is null for type={}, skipping", type);
            return;
        }

        // 1. Resolve channel preferences (or use defaults)
        NotificationPreferences prefs = preferencesRepository
                .findByUserIdAndNotificationType(recipientUserId, type)
                .orElse(defaultPrefs(recipientUserId, type));

        boolean inApp = prefs.isInAppEnabled();

        if (!inApp) {
            log.debug("Notification suppressed by user preferences — userId={}, type={}", recipientUserId, type);
            return;
        }

        // 2. Persist in-app record (always when inApp is enabled)
        Notification notification = notificationRepository.save(
                Notification.builder()
                        .userId(recipientUserId)
                        .type(type)
                        .title(title)
                        .body(body)
                        .referenceId(referenceId)
                        .referenceType(referenceType)
                        .build()
        );
        log.debug("Notification persisted — id={}, userId={}, type={}",
                notification.getId(), recipientUserId, type);

        // 3. Push via all enabled channels (e.g. WebSocket)
        NotificationResponse response = NotificationResponse.from(notification);
        for (NotificationSender sender : senders) {
            sender.send(recipientUserId, response);
        }

    }

    private NotificationPreferences defaultPrefs(UUID userId, NotificationType type) {
        return NotificationPreferences.builder()
                .userId(userId)
                .notificationType(type)
                .inAppEnabled(DEFAULT_IN_APP_ENABLED)
                .build();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void seedDefaultPreferences(UUID userId) {
        List<NotificationPreferences> existing = preferencesRepository.findAllByUserId(userId);
        List<NotificationType> existingTypes = existing.stream()
                .map(NotificationPreferences::getNotificationType)
                .toList();

        List<NotificationPreferences> toCreate = Arrays.stream(NotificationType.values())
                .filter(t -> !existingTypes.contains(t))
                .map(t -> NotificationPreferences.builder()
                        .userId(userId)
                        .notificationType(t)
                        .inAppEnabled(DEFAULT_IN_APP_ENABLED)
                        .build())
                .toList();

        if (!toCreate.isEmpty()) {
            preferencesRepository.saveAll(toCreate);
            log.debug("Seeded {} notification preference rows for userId={}", toCreate.size(), userId);
        }
    }
}