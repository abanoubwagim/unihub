package com.unihub.shared.api.external;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface CompanyApplicationApi {

    void submitApplication(UUID studentProfileId, UUID jobPostingId,
                           String cvUrl, UUID universityId, boolean isVerified);

    Page<?> getMyApplications(UUID studentProfileId, Pageable pageable);

    void reviewApplication(UUID companyId, UUID applicationId, boolean accepted);

    boolean hasStudentAlreadyApplied(UUID jobPostingId, UUID studentProfileId);
}