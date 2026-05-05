package com.unihub.student.application.impl;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.unihub.student.application.StudentProfileCreator;
import com.unihub.student.domain.enums.AcademicStatus;
import com.unihub.student.domain.model.StudentProfile;
import com.unihub.student.domain.repository.StudentProfileRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StudentProfileCreatorImpl implements StudentProfileCreator {

    private final StudentProfileRepository studentProfileRepository;

    private static final Logger log = LoggerFactory.getLogger(StudentProfileCreatorImpl.class);

    @Override
    @Transactional
    public void createEmptyProfile(UUID userId) {
        if (studentProfileRepository.existsByUserId(userId)) {
            log.debug("Profile already exists for userId={}, skipping creation", userId);
            return;
        }

        try {
            StudentProfile profile = new StudentProfile();
            profile.setUserId(userId);
            profile.setAcademicStatus(AcademicStatus.UNDERGRADUATE);
            studentProfileRepository.save(profile);
            log.info("Created empty student profile for userId={}", userId);
        } catch (DataIntegrityViolationException e) {
            // Profile already created by concurrent request
            log.warn("Concurrent profile creation detected for userId={} — already exists", userId);
        }
    }
}