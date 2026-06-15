package com.unihub.notifications.application.listener;

import com.unihub.chat.domain.event.ChatMessageSentEvent;
import com.unihub.notifications.application.service.NotificationDispatcher;
import com.unihub.notifications.domain.enums.NotificationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatNotificationListener {

    private final NotificationDispatcher dispatcher;

    @Async("taskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onChatMessageSent(ChatMessageSentEvent event) {
        log.debug("ChatNotificationListener triggered — conversationId={}, receiverId={}",
                event.conversationId(), event.receiverId());

        dispatcher.dispatch(
                event.receiverId(),
                NotificationType.CHAT_MESSAGE_RECEIVED,
                "New Message",
                "You have received a new message.",
                event.conversationId(),
                "CONVERSATION"
        );
    }
}