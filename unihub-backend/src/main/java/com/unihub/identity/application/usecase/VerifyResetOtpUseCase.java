package com.unihub.identity.application.usecase;

import com.unihub.identity.api.dto.req.VerifyResetOtpRequest;
import com.unihub.identity.api.dto.res.VerifyResetOtpResponse;

public interface VerifyResetOtpUseCase {

    VerifyResetOtpResponse verifyResetOtp(VerifyResetOtpRequest request);
}
