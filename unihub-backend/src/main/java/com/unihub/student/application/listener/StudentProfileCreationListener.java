package com.unihub.student.application.listener;

import com.unihub.identity.application.event.EmailVerifiedEvent;
import com.unihub.identity.domain.enums.Role;
import com.unihub.student.domain.enums.AcademicStatus;
import com.unihub.student.domain.model.StudentProfile;
import com.unihub.student.domain.repository.StudentProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class StudentProfileCreationListener {

    private final StudentProfileRepository studentProfileRepository;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void onEmailVerified(EmailVerifiedEvent event) {
        if (event.role() != Role.STUDENT) return;

        if (!studentProfileRepository.existsByUserId(event.userId())) {
            studentProfileRepository.save(
                    StudentProfile.builder()
                            .userId(event.userId())
                            .academicStatus(AcademicStatus.UNDERGRADUATE)
                            .createdAt(LocalDateTime.now())
                            .build()
            );
            log.info("Student profile created — userId={}", event.userId());
        }
    }
}