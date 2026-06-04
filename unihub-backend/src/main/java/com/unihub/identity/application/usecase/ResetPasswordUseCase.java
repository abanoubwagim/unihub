package com.unihub.identity.application.usecase;

import com.unihub.identity.api.dto.req.ResetPasswordRequest;

public interface ResetPasswordUseCase {
    void resetPassword(ResetPasswordRequest request);
}