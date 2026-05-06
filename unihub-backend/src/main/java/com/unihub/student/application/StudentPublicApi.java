package com.unihub.student.application;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;


public interface StudentPublicApi {

    Optional<StudentPublicInfo> getByUserId(UUID userId);

    Map<UUID, StudentPublicInfo> getByUserIds(Set<UUID> userIds);
}