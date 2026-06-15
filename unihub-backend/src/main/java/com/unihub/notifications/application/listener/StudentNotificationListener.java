package com.unihub.notifications.application.listener;

import com.unihub.company.domain.event.StudentHiredEvent;
import com.unihub.company.domain.event.StudentRejectedEvent;
import com.unihub.notifications.application.service.NotificationDispatcher;
import com.unihub.notifications.domain.enums.NotificationType;
import com.unihub.shared.api.external.StudentUserIdApi;
import com.unihub.student.domain.event.CertificateReviewedEvent;
import com.unihub.student.domain.event.StudentUniversitySetEvent;
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
public class StudentNotificationListener {

    private final NotificationDispatcher dispatcher;
    private final StudentUserIdApi studentUserIdApi;

    //  Job application accepted (StudentHiredEvent)
    @Async("taskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onStudentHired(StudentHiredEvent event) {
        UUID userId = resolveStudentUserId(event.studentProfileId());
        if (userId == null) return;

        dispatcher.dispatch(
                userId,
                NotificationType.JOB_APPLICATION_ACCEPTED,
                "Congratulations — Application Accepted!",
                "Your application has been accepted. The company will be in touch soon.",
                event.jobPostingId(),
                "JOB_APPLICATION"
        );
    }

    //  Job application rejected (StudentRejectedEvent)
    @Async("taskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onStudentRejected(StudentRejectedEvent event) {
        UUID userId = resolveStudentUserId(event.studentProfileId());
        if (userId == null) return;

        String body = event.rejectionReason() != null
                ? "Unfortunately, your application was not selected. Reason: " + event.rejectionReason()
                : "Unfortunately, your application was not selected. Keep applying!";

        dispatcher.dispatch(
                userId,
                NotificationType.JOB_APPLICATION_REJECTED,
                "Application Update",
                body,
                event.jobPostingId(),
                "JOB_APPLICATION"
        );
    }

    // Certificate reviewed

    @Async("taskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onCertificateReviewed(CertificateReviewedEvent event) {
        UUID userId = resolveStudentUserId(event.studentProfileId());
        if (userId == null) return;

        if (event.approved()) {
            dispatcher.dispatch(
                    userId,
                    NotificationType.CERTIFICATE_APPROVED,
                    "Graduation Certificate Approved",
                    "Your graduation certificate has been approved by your university.",
                    event.certId(),
                    "CERTIFICATE"
            );
        } else {
            String reason = event.rejectionReason() != null
                    ? " Reason: " + event.rejectionReason()
                    : "";
            dispatcher.dispatch(
                    userId,
                    NotificationType.CERTIFICATE_REJECTED,
                    "Graduation Certificate Rejected",
                    "Unfortunately, your graduation certificate was not approved." + reason,
                    event.certId(),
                    "CERTIFICATE"
            );
        }
    }

    // University linked

    @Async("taskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onUniversityLinked(StudentUniversitySetEvent event) {
        UUID userId = resolveStudentUserId(event.studentProfileId());
        if (userId == null) return;

        dispatcher.dispatch(
                userId,
                NotificationType.UNIVERSITY_LINKED,
                "University Account Linked",
                "You have been successfully linked to your university on UniHub.",
                event.universityId(),
                "UNIVERSITY"
        );
    }

    private UUID resolveStudentUserId(UUID profileId) {
        return studentUserIdApi.findUserIdByStudentProfileId(profileId)
                .orElseGet(() -> {
                    log.warn("StudentNotificationListener: could not resolve userId for studentProfileId={}",
                            profileId);
                    return null;
                });
    }
}