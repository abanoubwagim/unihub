package com.unihub.university.application.impl;

import com.unihub.student.domain.event.GraduationCertificateApprovedEvent;
import com.unihub.student.domain.event.StudentUniversitySetEvent;
import com.unihub.university.domain.repository.UniversityProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
@Transactional
public class UniversityStudentCountListener {

    private final UniversityProfileRepository universityProfileRepository;

    @EventListener
    public void onStudentUniversitySet(StudentUniversitySetEvent event) {
        log.debug("StudentUniversitySetEvent received — universityId={}", event.universityId());

        universityProfileRepository.findById(event.universityId())
                .ifPresentOrElse(
                        profile -> {
                            profile.setStudentCount(profile.getStudentCount() + 1);
                            universityProfileRepository.save(profile);
                            log.info("Student count incremented — universityId={}, newCount={}",
                                    event.universityId(), profile.getStudentCount());
                        },
                        () -> log.warn("University not found for studentCount increment — universityId={}",
                                event.universityId())
                );
    }

    @EventListener
    public void onGraduationCertificateApproved(GraduationCertificateApprovedEvent event) {
        if (event.universityId() == null) {
            log.warn("GraduationCertificateApprovedEvent received with null universityId — skipping graduate count");
            return;
        }

        log.debug("GraduationCertificateApprovedEvent received — universityId={}", event.universityId());

        universityProfileRepository.findById(event.universityId())
                .ifPresentOrElse(
                        profile -> {
                            profile.setGraduateCount(profile.getGraduateCount() + 1);
                            universityProfileRepository.save(profile);
                            log.info("Graduate count incremented — universityId={}, newCount={}",
                                    event.universityId(), profile.getGraduateCount());
                        },
                        () -> log.warn("University not found for graduateCount increment — universityId={}",
                                event.universityId())
                );
    }
}