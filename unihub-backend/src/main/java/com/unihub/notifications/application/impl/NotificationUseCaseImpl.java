package com.unihub.notifications.application.impl;

import com.unihub.notifications.api.dto.req.UpdatePreferencesRequest;
import com.unihub.notifications.api.dto.res.NotificationPreferencesResponse;
import com.unihub.notifications.api.dto.res.NotificationResponse;
import com.unihub.notifications.application.usecase.NotificationUseCase;
import com.unihub.notifications.domain.enums.NotificationType;
import com.unihub.notifications.domain.model.Notification;
import com.unihub.notifications.domain.model.NotificationPreferences;
import com.unihub.notifications.domain.repository.NotificationPreferencesRepository;
import com.unihub.notifications.domain.repository.NotificationRepository;
import com.unihub.shared.api.dto.PageResponse;
import com.unihub.shared.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationUseCaseImpl implements NotificationUseCase {

    private final NotificationRepository notificationRepository;
    private final NotificationPreferencesRepository preferencesRepository;


    @Override
    @Transactional(readOnly = true)
    public PageResponse<NotificationResponse> getNotifications(UUID userId, Pageable pageable) {
        return PageResponse.from(
                notificationRepository.findAllByUserId(userId, pageable)
                        .map(NotificationResponse::from)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadCount(UUID userId) {
        return notificationRepository.countByUserIdAndReadFalse(userId);
    }

    @Override
    @Transactional
    public NotificationResponse markRead(UUID userId, UUID notificationId) {
        Notification notification = notificationRepository
                .findByIdAndUserId(notificationId, userId)
                .orElseThrow(() -> new NotFoundException("Notification not found"));

        if (!notification.isRead()) {
            notification.markRead();
            notificationRepository.save(notification);
        }
        return NotificationResponse.from(notification);
    }

    @Override
    @Transactional
    public void markAllRead(UUID userId) {
        notificationRepository.markAllReadByUserId(userId);
        log.debug("Marked all notifications read for userId={}", userId);
    }

    @Override
    @Transactional
    public void deleteNotification(UUID userId, UUID notificationId) {
        // Verify ownership before deleting
        notificationRepository.findByIdAndUserId(notificationId, userId)
                .orElseThrow(() -> new NotFoundException("Notification not found"));
        notificationRepository.deleteByIdAndUserId(notificationId, userId);
    }


    @Override
    @Transactional(readOnly = true)
    public List<NotificationPreferencesResponse> getPreferences(UUID userId) {
        Map<NotificationType, NotificationPreferences> existing =
                preferencesRepository.findAllByUserId(userId)
                        .stream()
                        .collect(Collectors.toMap(
                                NotificationPreferences::getNotificationType,
                                p -> p));

        // Return a row for every NotificationType, falling back to defaults
        return Arrays.stream(NotificationType.values())
                .map(type -> {
                    NotificationPreferences p = existing.get(type);
                    return p != null
                            ? NotificationPreferencesResponse.from(p)
                            : NotificationPreferencesResponse.defaults(userId, type);
                })
                .toList();
    }

    @Override
    @Transactional
    public List<NotificationPreferencesResponse> updatePreferences(UUID userId,
                                                                   UpdatePreferencesRequest request) {
        // Single SELECT for all existing prefs
        Map<NotificationType, NotificationPreferences> existingMap =
                preferencesRepository.findAllByUserId(userId)
                        .stream()
                        .collect(Collectors.toMap(
                                NotificationPreferences::getNotificationType,
                                p -> p));

        List<NotificationPreferences> toSave = request.preferences().stream()
                .map(update -> {
                    NotificationPreferences prefs = existingMap.getOrDefault(
                            update.notificationType(),
                            NotificationPreferences.builder()
                                    .userId(userId)
                                    .notificationType(update.notificationType())
                                    .build()
                    );
                    prefs.setInAppEnabled(update.inAppEnabled());
                    return prefs;
                })
                .toList();

        // Single batch save
        preferencesRepository.saveAll(toSave);

        log.debug("Updated {} preference rows for userId={}", toSave.size(), userId);
        return getPreferences(userId);
    }
}