package com.unihub.company.application.listener;

import com.unihub.company.application.usecase.CompanyProfileCreator;
import com.unihub.identity.application.event.EmailVerifiedEvent;
import com.unihub.identity.domain.enums.Role;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class CompanyProfileCreationListener {

    private final CompanyProfileCreator companyProfileCreator;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void onEmailVerified(EmailVerifiedEvent event) {
        if (event.role() != Role.COMPANY) return;

        log.debug("EmailVerifiedEvent received for COMPANY — userId={}", event.userId());
        companyProfileCreator.createEmptyProfile(event.userId());
    }
}
