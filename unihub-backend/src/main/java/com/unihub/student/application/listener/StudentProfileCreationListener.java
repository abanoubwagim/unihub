package com.unihub.student.application.listener;

import com.unihub.identity.application.event.EmailVerifiedEvent;
import com.unihub.identity.domain.enums.Role;
import com.unihub.student.application.StudentProfileCreator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class StudentProfileCreationListener {

    private final StudentProfileCreator studentProfileCreator;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void onEmailVerified(EmailVerifiedEvent event) {
        if (event.role() != Role.STUDENT) return;

        log.debug("EmailVerifiedEvent received for STUDENT — userId={}", event.userId());
        studentProfileCreator.createEmptyProfile(event.userId());
    }
}