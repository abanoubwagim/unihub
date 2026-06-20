import { Injectable, inject, signal } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap, catchError, throwError } from 'rxjs';
import { environment } from '../../../environments/environment';
import * as M from '../models/auth.models';
import { TokenService } from './token.service';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);
  private readonly tokenService = inject(TokenService);

  private readonly BASE = `${environment.apiUrl}/auth`;

  readonly currentUser = signal<M.UserResponse | null>(null);
  readonly isAuthenticated = signal(this.tokenService.isTokenPresent());

  // Authentication
  login(body: M.LoginRequest): Observable<M.LoginResponse> {
    return this.http
      .post<M.LoginResponse>(`${this.BASE}/login`, body, { withCredentials: true })
      .pipe(tap(r => this.handleTokenResponse(r.accessToken)));
  }

  register(body: M.RegisterRequest): Observable<M.RegisterResponse> {
    return this.http.post<M.RegisterResponse>(`${this.BASE}/register`, body);
  }

  logout(): Observable<string> {
    return this.http
      .post(`${this.BASE}/logout`, {}, { withCredentials: true, responseType: 'text' })
      .pipe(tap(() => this.clearSession()));
  }

  refresh(): Observable<M.LoginResponse> {
    return this.http
      .post<M.LoginResponse>(`${this.BASE}/refresh`, {}, { withCredentials: true })
      .pipe(tap(r => this.handleTokenResponse(r.accessToken)));
  }

  // Current User
  getMe(): Observable<M.UserResponse> {
    return this.http
      .get<M.UserResponse>(`${this.BASE}/me`)
      .pipe(tap(user => this.currentUser.set(user)));
  }

  // Email Verification
  verifyEmail(body: M.VerifyEmailRequest): Observable<string> {
    return this.http
      .post(`${this.BASE}/verify-email`, body, { responseType: 'text' })
      .pipe(catchError(this.handleTextError));
  }

  resendVerification(body: M.ResendVerificationRequest): Observable<string> {
    return this.http
      .post(`${this.BASE}/resend-verification`, body, { responseType: 'text' })
      .pipe(catchError(this.handleTextError));
  }

  // Password Reset
  forgotPassword(body: M.ForgotPasswordRequest): Observable<string> {
    return this.http.post(`${this.BASE}/forgot-password`, body, { responseType: 'text' });
  }

  verifyResetOtp(body: M.VerifyResetOtpRequest): Observable<M.VerifyResetOtpResponse> {
    return this.http.post<M.VerifyResetOtpResponse>(`${this.BASE}/verify-reset-otp`, body);
  }

  resetPassword(body: M.ResetPasswordRequest): Observable<string> {
    return this.http.post(`${this.BASE}/reset-password`, body, { responseType: 'text' });
  }

  // OAuth2
  exchangeOAuth2Code(code: string): Observable<M.OAuth2TokenResponse> {
    return this.http
      .post<M.OAuth2TokenResponse>(`${this.BASE}/oauth2/token`, { code })
      .pipe(tap(r => this.handleTokenResponse(r.accessToken)));
  }

  getOAuth2LoginUrl(provider: string, role: M.Role): string {
    return `${environment.backendUrl}/oauth2/authorize/${provider}?role=${role}`;
  }

  // Helpers
  private handleTokenResponse(accessToken: string): void {
    this.tokenService.setAccessToken(accessToken);
    this.isAuthenticated.set(true);
  }

  private handleTextError(err: HttpErrorResponse): Observable<never> {
    let message = 'Something went wrong. Please try again.';
    if (typeof err.error === 'string') {
      try {
        const parsed = JSON.parse(err.error);
        message = parsed?.error ?? parsed?.error ?? message;
      } catch {}
    } else if (err.error) {
      message = err.error.message ?? err.error.error ?? message;
    }
    return throwError(() => ({ message } as M.ApiError));
  }

  clearSession(): void {
    this.tokenService.clearAccessToken();
    this.currentUser.set(null);
    this.isAuthenticated.set(false);
  }
}
