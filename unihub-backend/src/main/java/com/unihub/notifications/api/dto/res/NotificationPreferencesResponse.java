package com.unihub.notifications.api.dto.res;

import com.unihub.notifications.domain.enums.NotificationType;
import com.unihub.notifications.domain.model.NotificationPreferences;

import java.util.UUID;

public record NotificationPreferencesResponse(

        UUID userId,
        NotificationType notificationType,
        boolean inAppEnabled

) {

    public static NotificationPreferencesResponse from(NotificationPreferences p) {
        return new NotificationPreferencesResponse(
                p.getUserId(),
                p.getNotificationType(),
                p.isInAppEnabled()
        );
    }

    public static NotificationPreferencesResponse defaults(UUID userId, NotificationType type) {
        return new NotificationPreferencesResponse(userId, type, true);
    }
}