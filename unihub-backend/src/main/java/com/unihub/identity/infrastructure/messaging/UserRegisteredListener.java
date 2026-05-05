package com.unihub.identity.infrastructure.messaging;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.unihub.identity.domain.event.UserRegisteredEvent;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class UserRegisteredListener {


    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onUserRegistered(UserRegisteredEvent event) {
        log.info("User registered — userId={}, role={}", event.userId(), event.role());
    }
}