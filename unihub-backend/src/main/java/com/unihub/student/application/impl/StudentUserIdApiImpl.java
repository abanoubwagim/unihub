package com.unihub.student.application.impl;

import com.unihub.shared.api.external.StudentUserIdApi;
import com.unihub.student.domain.model.StudentProfile;
import com.unihub.student.domain.repository.StudentProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StudentUserIdApiImpl implements StudentUserIdApi {

    private final StudentProfileRepository studentProfileRepository;

    @Override
    @Transactional(readOnly = true)
    public Optional<UUID> findUserIdByStudentProfileId(UUID studentProfileId) {
        return studentProfileRepository.findById(studentProfileId)
                .map(StudentProfile::getUserId);
    }
}