package com.unihub.identity.application.usecase;

import com.unihub.identity.api.dto.VerifyResetOtpRequest;
import com.unihub.identity.api.dto.VerifyResetOtpResponse;

public interface VerifyResetOtpUseCase{

    VerifyResetOtpResponse verifyResetOtp(VerifyResetOtpRequest request);
}
