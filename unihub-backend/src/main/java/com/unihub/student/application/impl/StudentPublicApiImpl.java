package com.unihub.student.application.impl;


import com.unihub.shared.api.dto.external.StudentPublicInfo;
import com.unihub.shared.api.external.StudentPublicApi;
import com.unihub.student.domain.model.StudentProfile;
import com.unihub.student.domain.repository.StudentProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudentPublicApiImpl implements StudentPublicApi {

    private final StudentProfileRepository studentProfileRepository;


    @Override
    public Page<StudentPublicInfo> getStudentsByUniversityId(UUID universityId, Pageable pageable) {
        return studentProfileRepository.findPageByUniversityId(universityId, pageable)
                .map(this::toInfo);
    }


    private StudentPublicInfo toInfo(StudentProfile p) {
        return new StudentPublicInfo(
                p.getUserId(),
                p.getId(),
                p.getName(),
                p.getProfilePhotoUrl(),
                p.getMajorId(),
                p.getUniversityId(),
                p.getLevel() != null ? p.getLevel().name() : null);
    }
}