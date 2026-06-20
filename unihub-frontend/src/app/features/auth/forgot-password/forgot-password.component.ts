import { Component, inject, signal } from '@angular/core';
import {
  FormBuilder,
  FormGroup,
  Validators,
  AbstractControl,
  ValidationErrors,
  ReactiveFormsModule,
} from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../../core/services/auth.service';
import { ToastService } from '../../../core/services/toast.service';
import { ApiError } from '../../../core/models/auth.models';

function strongPassword(ctrl: AbstractControl): ValidationErrors | null {
  const v = ctrl.value as string;
  if (!v) return null;
  return /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])/.test(v)
    ? null
    : { weakPassword: true };
}

function passwordMatch(group: AbstractControl): ValidationErrors | null {
  const pw = group.get('newPassword')?.value;
  const cp = group.get('confirmPassword')?.value;
  return pw && cp && pw !== cp ? { passwordMismatch: true } : null;
}

type Step = 'EMAIL' | 'OTP' | 'PASSWORD';

@Component({
  selector: 'app-forgot-password',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './forgot-password.component.html',
  styleUrls: ['./forgot-password.component.scss'],
})
export class ForgotPasswordComponent {
  private fb = inject(FormBuilder);
  private authService = inject(AuthService);
  private router = inject(Router);
  private toast = inject(ToastService);

  readonly step = signal<Step>('EMAIL');
  readonly loading = signal(false);
  readonly resendCooldown = signal(0);

  email = '';
  resetToken = '';

  // Step 1: Email
  emailForm: FormGroup = this.fb.group({
    email: ['', [Validators.required, Validators.email]],
  });

  // Step 2: OTP
  otpForm: FormGroup = this.fb.group({
    otp: ['', [Validators.required, Validators.minLength(6), Validators.maxLength(6)]],
  });

  // Step 3: New password
  passwordForm: FormGroup = this.fb.group(
    {
      newPassword:     ['', [Validators.required, Validators.minLength(8), strongPassword]],
      confirmPassword: ['', Validators.required],
    },
    { validators: passwordMatch }
  );

  //  Step 1

  submitEmail(): void {
    this.emailForm.markAllAsTouched();
    if (this.emailForm.invalid || this.loading()) return;

    this.loading.set(true);
    this.email = this.emailForm.value.email;

    this.authService.forgotPassword({ email: this.email }).subscribe({
      next: () => {
        this.loading.set(false);
        this.toast.success('Reset code sent to your email.');
        this.step.set('OTP');
        this.startCooldown();
      },
      error: (err: ApiError) => {
        this.loading.set(false);
        // Always show success to avoid email enumeration
        this.toast.success('If this email is registered, a reset code was sent.');
        this.step.set('OTP');
        this.startCooldown();
      },
    });
  }

  //  Step 2

  submitOtp(): void {
    this.otpForm.markAllAsTouched();
    if (this.otpForm.invalid || this.loading()) return;

    this.loading.set(true);

    this.authService
      .verifyResetOtp({ email: this.email, otp: this.otpForm.value.otp })
      .subscribe({
        next: res => {
          this.loading.set(false);
          this.resetToken = res.resetToken;
          this.step.set('PASSWORD');
        },
        error: (err: ApiError) => {
          this.loading.set(false);
          this.toast.error(err.message);
        },
      });
  }

  resendOtp(): void {
    if (this.resendCooldown() > 0) return;
    this.authService.forgotPassword({ email: this.email }).subscribe({
      next: () => {
        this.toast.success('Reset code resent.');
        this.startCooldown();
      },
    });
  }

  onOtpInput(event: Event): void {
    const input = event.target as HTMLInputElement;
    input.value = input.value.replace(/\D/g, '').slice(0, 6);
    this.otpForm.get('otp')!.setValue(input.value, { emitEvent: false });
  }

  // Step 3

  submitPassword(): void {
    this.passwordForm.markAllAsTouched();
    if (this.passwordForm.invalid || this.loading()) return;

    this.loading.set(true);
    const { newPassword, confirmPassword } = this.passwordForm.value;

    this.authService
      .resetPassword({ resetToken: this.resetToken, newPassword, confirmPassword })
      .subscribe({
        next: () => {
          this.loading.set(false);
          this.toast.success('Password reset! Please sign in.');
          this.router.navigate(['/auth/login']);
        },
        error: (err: ApiError) => {
          this.loading.set(false);
          this.toast.error(err.message);
        },
      });
  }

  fieldError(form: FormGroup, field: string): string | null {
    const ctrl = form.get(field);
    if (!ctrl || !ctrl.touched || ctrl.valid) return null;
    if (field === 'confirmPassword') {
      if (ctrl.hasError('required')) return 'Please confirm your password';
      if (form.hasError('passwordMismatch')) return 'Passwords do not match';
      return null;
    }
    if (ctrl.hasError('required'))   return 'This field is required';
    if (ctrl.hasError('email'))      return 'Enter a valid email address';
    if (ctrl.hasError('minlength'))  {
      if (field === 'otp') return 'OTP must be exactly 6 digits';
      return 'Password must be at least 8 characters';
    }
    if (ctrl.hasError('weakPassword')) return 'Must include uppercase, lowercase, number, and symbol';
    return null;
  }

  private startCooldown(): void {
    this.resendCooldown.set(60);
    const t = setInterval(() => {
      this.resendCooldown.update(v => {
        if (v <= 1) { clearInterval(t); return 0; }
        return v - 1;
      });
    }, 1000);
  }
}
