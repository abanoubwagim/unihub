package com.unihub.identity.application.usecase;

import com.unihub.identity.api.dto.req.ResendVerificationRequest;

public interface ResendVerificationUseCase {

    void resendVerification(ResendVerificationRequest request);
}
