package com.unihub.shared.api.external;

import java.util.Optional;
import java.util.UUID;

public interface StudentUserIdApi {

    Optional<UUID> findUserIdByStudentProfileId(UUID studentProfileId);
}