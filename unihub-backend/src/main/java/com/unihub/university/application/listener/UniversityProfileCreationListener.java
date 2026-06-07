package com.unihub.university.application.listener;

import com.unihub.identity.application.event.EmailVerifiedEvent;
import com.unihub.identity.domain.enums.Role;
import com.unihub.university.application.UniversityProfileCreator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class UniversityProfileCreationListener {

    private final UniversityProfileCreator universityProfileCreator;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void onEmailVerified(EmailVerifiedEvent event) {
        if (event.role() != Role.UNIVERSITY) return;

        log.debug("EmailVerifiedEvent received for UNIVERSITY — userId={}", event.userId());
        universityProfileCreator.createEmptyProfile(event.userId());
    }
}
