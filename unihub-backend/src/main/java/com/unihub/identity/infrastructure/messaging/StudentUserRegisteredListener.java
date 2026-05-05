package com.unihub.identity.infrastructure.messaging;

import com.unihub.identity.domain.enums.Role;
import com.unihub.identity.domain.event.UserRegisteredEvent;
import com.unihub.student.application.StudentProfileCreator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class StudentUserRegisteredListener  {

    private final StudentProfileCreator studentProfileCreator;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onUserRegistered(UserRegisteredEvent event) {
        if (event.role() != Role.STUDENT) {
            return;
        }
        log.info("Creating student profile for new user — userId={}", event.userId());
        studentProfileCreator.createEmptyProfile(event.userId());
    }
}
