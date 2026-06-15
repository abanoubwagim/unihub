package com.unihub.notifications.infrastructure.channel;

import com.unihub.notifications.api.dto.res.NotificationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketNotificationSender implements NotificationSender {

    private static final String USER_QUEUE = "/queue/notifications";

    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public void send(UUID recipientUserId, NotificationResponse notification) {
        try {
            messagingTemplate.convertAndSendToUser(
                    recipientUserId.toString(),
                    USER_QUEUE,
                    notification
            );
            log.debug("WebSocket notification sent — userId={}, notificationId={}",
                    recipientUserId, notification.id());
        } catch (Exception e) {
            log.warn("WebSocket send failed (user likely offline) — userId={}, type={}",
                    recipientUserId, notification.type());
        }
    }
}