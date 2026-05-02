package com.unihub.student.application.impl;

import java.util.UUID;

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

    @Override
    @Transactional
    public void createEmptyProfile(UUID userId) {
        if (studentProfileRepository.existsByUserId(userId)) return;

        StudentProfile profile = new StudentProfile();
        profile.setUserId(userId);
        profile.setAcademicStatus(AcademicStatus.UNDERGRADUATE);
        studentProfileRepository.save(profile);
    }
}