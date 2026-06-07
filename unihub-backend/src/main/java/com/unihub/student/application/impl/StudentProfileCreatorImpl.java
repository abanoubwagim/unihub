package com.unihub.student.application.impl;

import com.unihub.student.application.StudentProfileCreator;
import com.unihub.student.domain.model.StudentProfile;
import com.unihub.student.domain.repository.StudentProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class StudentProfileCreatorImpl implements StudentProfileCreator {

    private final StudentProfileRepository studentProfileRepository;

    @Override
    @Transactional
    public void createEmptyProfile(UUID userId) {
        if (studentProfileRepository.existsByUserId(userId)) {
            log.debug("Student profile already exists for userId={}, skipping", userId);
            return;
        }
        try {
            StudentProfile profile = new StudentProfile();
            profile.setUserId(userId);
            studentProfileRepository.save(profile);
            log.info("Created empty student profile for userId={}", userId);
        } catch (DataIntegrityViolationException e) {
            log.warn("Concurrent student profile creation for userId={} — already exists", userId);
        }
    }
}