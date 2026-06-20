import { Component, inject, signal } from '@angular/core';
import {
  FormBuilder,
  FormGroup,
  Validators,
  ReactiveFormsModule,
} from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../../core/services/auth.service';
import { ToastService } from '../../../core/services/toast.service';
import { ApiError, Role } from '../../../core/models/auth.models';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss'],
})
export class LoginComponent {
  private fb         = inject(FormBuilder);
  private authService = inject(AuthService);
  private router      = inject(Router);
  private toast       = inject(ToastService);

  readonly loading      = signal(false);
  readonly showPassword = signal(false);
  readonly oauthRole    = signal<Role>('STUDENT');

  // Email-verification state
  readonly emailUnverified = signal(false);
  readonly resendLoading   = signal(false);
  readonly resendSent      = signal(false);

  form: FormGroup = this.fb.group({
    email:    ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required]],
  });

  get email()    { return this.form.get('email')!; }
  get password() { return this.form.get('password')!; }

  // Validation helpers
  fieldError(field: 'email' | 'password'): string | null {
    const ctrl = this.form.get(field)!;
    if (!ctrl.touched || ctrl.valid) return null;
    if (ctrl.hasError('required')) return 'This field is required';
    if (ctrl.hasError('email'))    return 'Enter a valid email address';
    return null;
  }

  togglePassword(): void {
    this.showPassword.update(v => !v);
  }

  // Login
  onSubmit(): void {
    this.form.markAllAsTouched();
    if (this.form.invalid || this.loading()) return;

    // Reset verification state on every new attempt
    this.emailUnverified.set(false);
    this.resendSent.set(false);
    this.loading.set(true);

    this.authService.login(this.form.getRawValue()).subscribe({
      next: () => {
        this.authService.getMe().subscribe({
          next: () => {
            this.loading.set(false);
            this.router.navigate(['/dashboard']);
          },
          error: () => {
            this.loading.set(false);
            this.router.navigate(['/dashboard']);
          },
        });
      },
      error: (err: ApiError) => {
        this.loading.set(false);
        if (this.isEmailNotVerifiedError(err)) {
          // Show the inline verification banner instead of a toast
          this.emailUnverified.set(true);
        } else {
          this.toast.error(err.message);
        }
      },
    });
  }

  // Resend verification email
  resendVerification(): void {
    if (this.resendLoading() || this.resendSent()) return;

    const email = this.email.value as string;
    this.resendLoading.set(true);

    this.authService.resendVerification({email}).subscribe({
      next: () => {
        this.resendLoading.set(false);
        this.resendSent.set(true);
      },
      error: (err: ApiError) => {
        this.resendLoading.set(false);
        this.toast.error(err.message ?? 'Failed to resend verification email. Please try again.');
      },
    });
  }

  // OAuth2
  loginWithGoogle(): void {
    const url = this.authService.getOAuth2LoginUrl('google', this.oauthRole());
    window.location.href = url;
  }

  // Private helpers
  private isEmailNotVerifiedError(err: ApiError): boolean {
    const msg  = (err.message  ?? '').toLowerCase();
    const code = ((err as any).code ?? '').toLowerCase();

    return (
      msg.includes('not verified')        ||
      msg.includes('not activated')       ||
      msg.includes('verify your email')   ||
      msg.includes('email not confirmed') ||
      code.includes('email_not_verified') ||
      code.includes('account_not_activated')
    );
  }
}
