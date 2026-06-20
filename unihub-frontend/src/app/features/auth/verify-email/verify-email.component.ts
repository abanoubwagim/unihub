import { Component, inject, signal, OnInit } from '@angular/core';
import {
  FormBuilder,
  FormGroup,
  Validators,
  ReactiveFormsModule,
} from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../../core/services/auth.service';
import { ToastService } from '../../../core/services/toast.service';
import { ApiError } from '../../../core/models/auth.models';

const OTP_RESEND_COOLDOWN = 60; // seconds

@Component({
  selector: 'app-verify-email',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './verify-email.component.html',
  styleUrls: ['./verify-email.component.scss'],
})
export class VerifyEmailComponent implements OnInit {
  private fb = inject(FormBuilder);
  private authService = inject(AuthService);
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private toast = inject(ToastService);

  readonly loading = signal(false);
  readonly resending = signal(false);
  readonly resendCooldown = signal(0);

  email = '';

  form: FormGroup = this.fb.group({
    otp: ['', [Validators.required, Validators.minLength(6), Validators.maxLength(6)]],
  });

  ngOnInit(): void {
    this.email = this.route.snapshot.queryParamMap.get('email') ?? '';
    if (!this.email) {
      this.router.navigate(['/auth/register']);
    }
  }

  get otp() { return this.form.get('otp')!; }

  get otpError(): string | null {
    if (!this.otp.touched || this.otp.valid) return null;
    if (this.otp.hasError('required')) return 'OTP is required';
    if (this.otp.hasError('minlength') || this.otp.hasError('maxlength'))
      return 'OTP must be exactly 6 digits';
    return null;
  }

  onOtpInput(event: Event): void {
    const input = event.target as HTMLInputElement;
    input.value = input.value.replace(/\D/g, '').slice(0, 6);
    this.otp.setValue(input.value, { emitEvent: false });
  }

  onSubmit(): void {
    this.form.markAllAsTouched();
    if (this.form.invalid || this.loading()) return;

    this.loading.set(true);

    this.authService.verifyEmail({ email: this.email, otp: this.otp.value }).subscribe({
      next: () => {
        this.loading.set(false);
        this.toast.success('Email verified! You can now sign in.');
        this.router.navigate(['/auth/login']);
      },
      error: (err: ApiError) => {
        this.loading.set(false);
        this.toast.error(err.message);
      },
    });
  }

  resend(): void {
    if (this.resendCooldown() > 0 || this.resending()) return;

    this.resending.set(true);

    this.authService.resendVerification({ email: this.email }).subscribe({
      next: () => {
        this.resending.set(false);
        this.toast.success('Verification code resent. Check your inbox.');
        this.startCooldown();
      },
      error: (err: ApiError) => {
        this.resending.set(false);
        this.toast.error(err.message);
      },
    });
  }

  private startCooldown(): void {
    this.resendCooldown.set(OTP_RESEND_COOLDOWN);
    const interval = setInterval(() => {
      this.resendCooldown.update(v => {
        if (v <= 1) { clearInterval(interval); return 0; }
        return v - 1;
      });
    }, 1000);
  }
}
