package com.unihub.identity.application;

import com.unihub.identity.api.dto.ResetPasswordRequest;

public interface ResetPasswordUseCase {
    void resetPassword(ResetPasswordRequest request);
}