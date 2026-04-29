package com.unihub.identity.application;

import com.unihub.identity.api.dto.ForgotPasswordRequest;

public interface ForgotPasswordUseCase {

    void forgotPassword(ForgotPasswordRequest request);
}
