package com.unihub.student.application.listener;

import com.unihub.shared.events.UserDeletedEvent;
import com.unihub.student.domain.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class StudentUserDeletedListener {

    private final StudentProfileRepository profileRepository;
    private final StudentExperienceRepository experienceRepository;
    private final StudentProjectRepository projectRepository;
    private final StudentCertificationRepository certificationRepository;
    private final GraduationCertificateRepository gradCertRepository;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onUserDeleted(UserDeletedEvent event) {
        profileRepository.findByUserId(event.userId()).ifPresent(profile -> {
            UUID profileId = profile.getId();
            log.info("Cleaning up student data for userId={}, profileId={}", event.userId(), profileId);

            certificationRepository.deleteAllByStudent_Id(profileId);
            projectRepository.deleteAllByStudent_Id(profileId);
            experienceRepository.deleteAllByStudent_Id(profileId);
            gradCertRepository.deleteAllByStudentId(profileId);

            // Deleting the profile entity cascades to StudentLink rows
            profileRepository.delete(profile);

            log.info("Student cleanup complete for userId={}", event.userId());
        });
    }
}