package com.unihub.identity.infrastructure.messaging;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.unihub.identity.domain.enums.Role;
import com.unihub.identity.domain.event.UserRegisteredEvent;
import com.unihub.student.application.StudentProfileCreator;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UserRegisteredListener {

    private final StudentProfileCreator studentProfileCreator;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onUserRegistered(UserRegisteredEvent event) {
        if (event.role() == Role.STUDENT) {
            studentProfileCreator.createEmptyProfile(event.userId());
        }
    }
}