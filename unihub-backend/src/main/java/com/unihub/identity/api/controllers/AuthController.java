package com.unihub.identity.api.controllers;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.unihub.identity.api.dto.ExchangeOAuth2CodeRequest;
import com.unihub.identity.api.dto.ForgotPasswordRequest;
import com.unihub.identity.api.dto.LoginRequest;
import com.unihub.identity.api.dto.LoginResponse;
import com.unihub.identity.api.dto.OAuth2TokenResponse;
import com.unihub.identity.api.dto.RegisterRequest;
import com.unihub.identity.api.dto.RegisterResponse;
import com.unihub.identity.api.dto.ResendVerificationRequest;
import com.unihub.identity.api.dto.ResetPasswordRequest;
import com.unihub.identity.api.dto.UserResponse;
import com.unihub.identity.api.dto.VerifyEmailRequest;
import com.unihub.identity.api.dto.VerifyResetOtpRequest;
import com.unihub.identity.api.dto.VerifyResetOtpResponse;
import com.unihub.identity.application.usecase.ExchangeOAuth2CodeUseCase;
import com.unihub.identity.application.usecase.ForgotPasswordUseCase;
import com.unihub.identity.application.usecase.GetCurrentUserUseCase;
import com.unihub.identity.application.usecase.LoginUserUseCase;
import com.unihub.identity.application.usecase.LogoutUseCase;
import com.unihub.identity.application.usecase.RegisterUserUseCase;
import com.unihub.identity.application.usecase.ResendVerificationUseCase;
import com.unihub.identity.application.usecase.ResetPasswordUseCase;
import com.unihub.identity.application.usecase.VerifyEmailUseCase;
import com.unihub.identity.application.usecase.VerifyResetOtpUseCase;

import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Validated
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final LoginUserUseCase loginUserUseCase;
    private final RegisterUserUseCase registerUserUseCase;
    private final VerifyEmailUseCase verifyEmailUseCase;
    private final ResendVerificationUseCase resendVerificationUseCase;
    private final GetCurrentUserUseCase getCurrentUserUseCase;
    private final ForgotPasswordUseCase forgotPasswordUseCase;
    private final ResetPasswordUseCase resetPasswordUseCase;
    private final VerifyResetOtpUseCase verifyResetOtpUseCase;
    private final LogoutUseCase logoutUseCase;
    private final ExchangeOAuth2CodeUseCase exchangeOAuth2CodeUseCase;

    @PostMapping("/login")
    public LoginResponse login(
            @Valid @RequestBody LoginRequest request) {
        return loginUserUseCase.login(request);
    }

    @PostMapping("/register")
    public RegisterResponse register(
            @Valid @RequestBody RegisterRequest request) {
        return registerUserUseCase.register(request);
    }

    @PostMapping("/verify-email")
    public ResponseEntity<String> verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
        verifyEmailUseCase.verifyEmail(request);
        return ResponseEntity.ok("Email verified successfully");
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<String> resendVerification(@Valid @RequestBody ResendVerificationRequest request) {
        resendVerificationUseCase.resendVerification(request);
        return ResponseEntity.ok("Verification code sent. Please check your email");
    }

    @GetMapping("/me")
    public UserResponse me(Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        return getCurrentUserUseCase.getCurrentUser(userId);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        forgotPasswordUseCase.forgotPassword(request);
        return ResponseEntity.ok("If this email is registered, a reset code will be sent");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        resetPasswordUseCase.resetPassword(request);
        return ResponseEntity.ok("Password reset successfully");
    }

    @PostMapping("/verify-reset-otp")
    public VerifyResetOtpResponse verifyResetOtp(@Valid @RequestBody VerifyResetOtpRequest request) {
        return verifyResetOtpUseCase.verifyResetOtp(request);
    }

    @PostMapping("/oauth2/token")
    public OAuth2TokenResponse exchangeOAuth2Code(
            @Valid @RequestBody ExchangeOAuth2CodeRequest request) {
        return exchangeOAuth2CodeUseCase.exchange(request.code());
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            logoutUseCase.logout(authHeader.substring(7));
        }
        return ResponseEntity.ok("Logged out successfully");
    }

}
