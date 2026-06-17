package com.unihub.shared.api.external;

import java.util.Optional;
import java.util.UUID;

public interface UniversityUserIdApi {

    Optional<UUID> findUserIdByUniversityProfileId(UUID universityProfileId);
}