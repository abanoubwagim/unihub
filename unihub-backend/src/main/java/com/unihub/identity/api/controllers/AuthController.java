package com.unihub.identity.api.controllers;

import com.unihub.identity.api.dto.*;
import com.unihub.identity.application.usecase.*;
import com.unihub.shared.exception.InvalidTokenException;
import com.unihub.shared.exception.SecurityViolationException;
import com.unihub.shared.util.CookieUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@Validated
@RestController
@RequestMapping("/api/v1/auth")
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
    private final TokenRotationUseCase tokenRotationUseCase;
    private final ExchangeOAuth2CodeUseCase exchangeOAuth2CodeUseCase;

    @Value("${jwt.refresh-expiration-seconds}")
    private long refreshExpirationSeconds;


    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {

        AuthenticationResult result = loginUserUseCase.login(request);

        ResponseCookie refreshCookie =
                CookieUtil.buildRefreshCookie(result.rawRefreshToken(), refreshExpirationSeconds);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(LoginResponse.from(result));
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refresh(HttpServletRequest request) {

        String rawRefreshToken = CookieUtil.extractRefreshToken(request)
                .orElseThrow(() -> new InvalidTokenException("Refresh token cookie is missing"));

        AuthenticationResult result;
        try {
            result = tokenRotationUseCase.refresh(rawRefreshToken);
        } catch (SecurityViolationException | InvalidTokenException e) {
            ResponseCookie expiredCookie = CookieUtil.buildExpiredRefreshCookie();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .header(HttpHeaders.SET_COOKIE, expiredCookie.toString())
                    .build();
        }

        // Rotation succeeded — send rotated refresh token as new cookie
        ResponseCookie newRefreshCookie =
                CookieUtil.buildRefreshCookie(result.rawRefreshToken(), refreshExpirationSeconds);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, newRefreshCookie.toString())
                .body(LoginResponse.from(result));
    }


    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request) {

        String rawAccessToken = extractBearerToken(request);
        String rawRefreshToken = CookieUtil.extractRefreshToken(request).orElse(null);

        if (rawAccessToken != null || rawRefreshToken != null) {
            logoutUseCase.logout(rawAccessToken, rawRefreshToken);
        }

        // Clear the cookie regardless of whether tokens were found
        ResponseCookie expiredCookie = CookieUtil.buildExpiredRefreshCookie();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, expiredCookie.toString())
                .body("Logged out successfully");
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        RegisterResponse response = registerUserUseCase.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
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
    public ResponseEntity<UserResponse> me(Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        return ResponseEntity.ok(getCurrentUserUseCase.getCurrentUser(userId));
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
    public ResponseEntity<VerifyResetOtpResponse> verifyResetOtp(
            @Valid @RequestBody VerifyResetOtpRequest request) {
        return ResponseEntity.ok(verifyResetOtpUseCase.verifyResetOtp(request));
    }

    @PostMapping("/oauth2/token")
    public ResponseEntity<OAuth2TokenResponse> exchangeOAuth2Code(
            @Valid @RequestBody ExchangeOAuth2CodeRequest request) {
        return ResponseEntity.ok(exchangeOAuth2CodeUseCase.exchange(request.code()));
    }

    private String extractBearerToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

}
