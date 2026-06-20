// Enums
export type Role = 'STUDENT' | 'COMPANY' | 'UNIVERSITY' | 'ADMIN';
export type UserStatus = 'PENDING' | 'ACTIVE' | 'SUSPENDED' | 'BANNED';

// Requests
export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  email: string;
  password: string;
  confirmPassword: string;
  role: Role;
}

export interface VerifyEmailRequest {
  email: string;
  otp: string;
}

export interface ResendVerificationRequest {
  email: string;
}

export interface ForgotPasswordRequest {
  email: string;
}

export interface VerifyResetOtpRequest {
  email: string;
  otp: string;
}

export interface ResetPasswordRequest {
  resetToken: string;
  newPassword: string;
  confirmPassword: string;
}

export interface ExchangeOAuth2CodeRequest {
  code: string;
}

export interface DeleteAccountRequest {
  password: string;
}

// Responses
export interface LoginResponse {
  accessToken: string;
  tokenType: string;
  expiresIn: number;
}

export interface RegisterResponse {
  userId: string;
  email: string;
  role: Role;
  status: UserStatus;
}

export interface UserResponse {
  id: string;
  email: string;
  role: Role;
  status: UserStatus;
  emailVerified: boolean;
}

export interface VerifyResetOtpResponse {
  resetToken: string;
}

export interface OAuth2TokenResponse {
  accessToken: string;
  tokenType: string;
}

// API Error
export interface ApiError {
  status: number;
  message: string;
}
