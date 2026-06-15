package com.unihub.notifications.infrastructure.channel;

import com.unihub.notifications.api.dto.res.NotificationResponse;

import java.util.UUID;

public interface NotificationSender {

    void send(UUID recipientUserId, NotificationResponse notification);
}