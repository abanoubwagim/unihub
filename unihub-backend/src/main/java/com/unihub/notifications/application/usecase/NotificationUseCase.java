package com.unihub.notifications.application.usecase;

import com.unihub.notifications.api.dto.req.UpdatePreferencesRequest;
import com.unihub.notifications.api.dto.res.NotificationPreferencesResponse;
import com.unihub.notifications.api.dto.res.NotificationResponse;
import com.unihub.shared.api.dto.PageResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface NotificationUseCase {

    PageResponse<NotificationResponse> getNotifications(UUID userId, Pageable pageable);

    long getUnreadCount(UUID userId);

    NotificationResponse markRead(UUID userId, UUID notificationId);

    void markAllRead(UUID userId);

    void deleteNotification(UUID userId, UUID notificationId);

    List<NotificationPreferencesResponse> getPreferences(UUID userId);

    List<NotificationPreferencesResponse> updatePreferences(UUID userId, UpdatePreferencesRequest request);
}