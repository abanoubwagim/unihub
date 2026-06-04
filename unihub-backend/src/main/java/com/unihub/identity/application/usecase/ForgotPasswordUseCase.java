package com.unihub.identity.application.usecase;

import com.unihub.identity.api.dto.req.ForgotPasswordRequest;

public interface ForgotPasswordUseCase {

    void forgotPassword(ForgotPasswordRequest request);
}
