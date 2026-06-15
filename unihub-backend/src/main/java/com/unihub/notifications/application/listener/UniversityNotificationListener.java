package com.unihub.notifications.application.listener;

import com.unihub.notifications.application.service.NotificationDispatcher;
import com.unihub.notifications.domain.enums.NotificationType;
import com.unihub.shared.api.external.UniversityUserIdApi;
import com.unihub.student.domain.event.GraduationCertificateSubmittedEvent;
import com.unihub.university.domain.event.UniversityPartnershipRequestedEvent;
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
public class UniversityNotificationListener {

    private final NotificationDispatcher dispatcher;
    private final UniversityUserIdApi universityUserIdApi;

    // New graduation certificate needs review

    @Async("taskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onCertificateSubmitted(GraduationCertificateSubmittedEvent event) {
        UUID userId = resolveUniversityUserId(event.universityId());
        if (userId == null) return;

        dispatcher.dispatch(
                userId,
                NotificationType.CERTIFICATE_SUBMITTED,
                "New Graduation Certificate Awaiting Review",
                "A student has submitted a graduation certificate for your university. " +
                        "This is attempt #" + event.attemptNumber() + ".",
                event.certId(),
                "CERTIFICATE"
        );
    }

    // Company sent a partnership request

    @Async("taskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onPartnershipRequested(UniversityPartnershipRequestedEvent event) {
        UUID userId = resolveUniversityUserId(event.universityId());
        if (userId == null) return;

        dispatcher.dispatch(
                userId,
                NotificationType.PARTNERSHIP_REQUESTED,
                "New Partnership Request",
                "A company has sent you a partnership request on UniHub.",
                event.partnershipId(),
                "PARTNERSHIP"
        );
    }

    private UUID resolveUniversityUserId(UUID profileId) {
        return universityUserIdApi.findUserIdByUniversityProfileId(profileId)
                .orElseGet(() -> {
                    log.warn("UniversityNotificationListener: could not resolve userId for universityProfileId={}",
                            profileId);
                    return null;
                });
    }
}