package com.unihub.identity.application;

import com.unihub.identity.api.dto.ResendVerificationRequest;

public interface ResendVerificationUseCase {

    void resendVerification(ResendVerificationRequest request);
}
