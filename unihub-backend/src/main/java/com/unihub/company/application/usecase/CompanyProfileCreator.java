package com.unihub.company.application.usecase;

import java.util.UUID;

public interface CompanyProfileCreator {
    void createEmptyProfile(UUID userId);
}