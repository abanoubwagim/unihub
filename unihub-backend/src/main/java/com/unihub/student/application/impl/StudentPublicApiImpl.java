package com.unihub.student.application.impl;

import com.unihub.student.application.StudentPublicApi;
import com.unihub.student.application.StudentPublicInfo;
import com.unihub.student.domain.model.StudentProfile;
import com.unihub.student.domain.repository.StudentProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudentPublicApiImpl implements StudentPublicApi {

    private final StudentProfileRepository studentProfileRepository;

    @Override
    public Optional<StudentPublicInfo> getByUserId(UUID userId) {
        return studentProfileRepository.findByUserId(userId)
                .map(this::toInfo);
    }

    @Override
    public Map<UUID, StudentPublicInfo> getByUserIds(Set<UUID> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Map.of();
        }
        return studentProfileRepository.findAllByUserIdIn(userIds).stream()
                .collect(Collectors.toMap(
                        StudentProfile::getUserId,
                        this::toInfo));
    }

    private StudentPublicInfo toInfo(StudentProfile p) {
        return new StudentPublicInfo(
                p.getUserId(),
                p.getId(),
                p.getName(),
                p.getProfilePhotoUrl(),
                p.getMajorId());
    }
}