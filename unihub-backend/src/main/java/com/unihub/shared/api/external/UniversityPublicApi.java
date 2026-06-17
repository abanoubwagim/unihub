package com.unihub.shared.api.external;


import com.unihub.shared.api.dto.external.UniversityPublicInfo;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface UniversityPublicApi {

    Optional<UniversityPublicInfo> getByUserId(UUID userId);

    Optional<UniversityPublicInfo> getByProfileId(UUID profileId);

    Map<UUID, UniversityPublicInfo> getByProfileIds(Set<UUID> profileIds);

    boolean isMajorOfferedByUniversity(UUID universityId, UUID majorId);


}