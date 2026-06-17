package com.unihub.shared.api.external;

import java.util.Optional;
import java.util.UUID;

public interface CompanyUserIdApi {

    Optional<UUID> findUserIdByCompanyProfileId(UUID companyProfileId);
}