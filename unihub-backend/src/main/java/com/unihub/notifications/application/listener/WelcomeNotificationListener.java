package com.unihub.notifications.application.listener;

import com.unihub.identity.application.event.EmailVerifiedEvent;
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
public class WelcomeNotificationListener {

    private static final String WELCOME_TITLE =
            "Welcome to UniHub! 🎉";

    private static final String WELCOME_BODY =
            "Your account is verified and ready.";

    private final NotificationDispatcher dispatcher;

    @Async("taskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onEmailVerified(EmailVerifiedEvent event) {
        log.info("Dispatching welcome notification — userId={}", event.userId());
        dispatcher.dispatch(
                event.userId(),
                NotificationType.WELCOME,
                WELCOME_TITLE,
                WELCOME_BODY,
                null,          // no referenceId — this is a system-level message
                "SYSTEM"
        );
    }
}