package com.unihub.notifications.application.listener;

import com.unihub.company.domain.event.JobApplicationSubmittedEvent;
import com.unihub.notifications.application.service.NotificationDispatcher;
import com.unihub.notifications.domain.enums.NotificationType;
import com.unihub.shared.api.external.CompanyUserIdApi;
import com.unihub.university.domain.event.UniversityPartnershipAcceptedEvent;
import com.unihub.university.domain.event.UniversityPartnershipRejectedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class CompanyNotificationListener {

    private final NotificationDispatcher dispatcher;
    private final CompanyUserIdApi companyUserIdApi;

    // New job application received

    @Async("taskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onJobApplicationReceived(JobApplicationSubmittedEvent event) {
        UUID userId = resolveCompanyUserId(event.companyProfileId());
        if (userId == null) return;

        dispatcher.dispatch(
                userId,
                NotificationType.JOB_APPLICATION_RECEIVED,
                "New Job Application Received",
                "A student has applied to one of your job postings. Review their application now.",
                event.jobPostingId(),
                "JOB_APPLICATION"
        );
    }

    // Partnership accepted by university

    @Async("taskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onPartnershipAccepted(UniversityPartnershipAcceptedEvent event) {
        UUID userId = resolveCompanyUserId(event.companyId());
        if (userId == null) return;

        dispatcher.dispatch(
                userId,
                NotificationType.PARTNERSHIP_ACCEPTED,
                "Partnership Request Accepted",
                "A university has accepted your partnership request on UniHub.",
                event.partnershipId(),
                "PARTNERSHIP"
        );
    }

    // Partnership rejected by university

    @Async("taskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onPartnershipRejected(UniversityPartnershipRejectedEvent event) {
        UUID userId = resolveCompanyUserId(event.companyId());
        if (userId == null) return;

        dispatcher.dispatch(
                userId,
                NotificationType.PARTNERSHIP_REJECTED,
                "Partnership Request Declined",
                "A university has declined your partnership request.",
                event.partnershipId(),
                "PARTNERSHIP"
        );
    }


    private UUID resolveCompanyUserId(UUID profileId) {
        return companyUserIdApi.findUserIdByCompanyProfileId(profileId)
                .orElseGet(() -> {
                    log.warn("CompanyNotificationListener: could not resolve userId for companyProfileId={}",
                            profileId);
                    return null;
                });
    }
}