package com.unihub.identity.application.usecase;

import com.unihub.identity.api.dto.ResetPasswordRequest;

public interface ResetPasswordUseCase {
    void resetPassword(ResetPasswordRequest request);
}