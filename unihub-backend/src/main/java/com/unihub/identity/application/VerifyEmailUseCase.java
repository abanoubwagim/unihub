package com.unihub.identity.application;

import com.unihub.identity.api.dto.VerifyEmailRequest;

public interface VerifyEmailUseCase {
    void verifyEmail(VerifyEmailRequest request);
}
