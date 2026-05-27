package com.unihub.student.application;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;


public interface StudentPublicApi {

    Optional<StudentPublicInfo> getByUserId(UUID userId);

    Map<UUID, StudentPublicInfo> getByUserIds(Set<UUID> userIds);

    Page<StudentPublicInfo> getStudentsByUniversityId(UUID universityId, Pageable pageable);

}