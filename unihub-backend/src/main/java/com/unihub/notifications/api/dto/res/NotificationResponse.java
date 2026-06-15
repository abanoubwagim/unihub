package com.unihub.notifications.api.dto.res;

import com.unihub.notifications.domain.enums.NotificationType;
import com.unihub.notifications.domain.model.Notification;

import java.time.LocalDateTime;
import java.util.UUID;

public record NotificationResponse(

        UUID id,
        NotificationType type,
        String title,
        String body,
        UUID referenceId,
        String referenceType,
        boolean read,
        LocalDateTime createdAt,
        LocalDateTime readAt
) {

    public static NotificationResponse from(Notification n) {
        return new NotificationResponse(
                n.getId(),
                n.getType(),
                n.getTitle(),
                n.getBody(),
                n.getReferenceId(),
                n.getReferenceType(),
                n.isRead(),
                n.getCreatedAt(),
                n.getReadAt()
        );
    }
}