import { Component, inject, signal, OnInit } from '@angular/core';
import {
  FormBuilder,
  FormGroup,
  Validators,
  AbstractControl,
  ValidationErrors,
  ReactiveFormsModule,
} from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../../core/services/auth.service';
import { ToastService } from '../../../core/services/toast.service';
import { ApiError } from '../../../core/models/auth.models';

function strongPassword(ctrl: AbstractControl): ValidationErrors | null {
  const v = ctrl.value as string;
  if (!v) return null;
  return /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])/.test(v)
    ? null : { weakPassword: true };
}

function passwordMatch(group: AbstractControl): ValidationErrors | null {
  const pw = group.get('newPassword')?.value;
  const cp = group.get('confirmPassword')?.value;
  return pw && cp && pw !== cp ? { passwordMismatch: true } : null;
}

@Component({
  selector: 'app-reset-password',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './reset-password.component.html',
  styleUrls: ['./reset-password.component.scss'],
})
export class ResetPasswordComponent implements OnInit {
  private fb = inject(FormBuilder);
  private authService = inject(AuthService);
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private toast = inject(ToastService);

  readonly loading = signal(false);
  readonly showPassword = signal(false);
  readonly showConfirm = signal(false);

  resetToken = '';

  form: FormGroup = this.fb.group(
    {
      newPassword:     ['', [Validators.required, Validators.minLength(8), strongPassword]],
      confirmPassword: ['', Validators.required],
    },
    { validators: passwordMatch }
  );

  ngOnInit(): void {
    // Token can come from query params if navigated directly
    this.resetToken = this.route.snapshot.queryParamMap.get('token') ?? '';
    if (!this.resetToken) {
      this.router.navigate(['/auth/forgot-password']);
    }
  }

  fieldError(field: 'newPassword' | 'confirmPassword'): string | null {
    const ctrl = this.form.get(field)!;
    if (!ctrl.touched) return null;
    if (field === 'confirmPassword') {
      if (ctrl.hasError('required')) return 'Please confirm your password';
      if (this.form.hasError('passwordMismatch')) return 'Passwords do not match';
      return null;
    }
    if (ctrl.hasError('required'))   return 'Password is required';
    if (ctrl.hasError('minlength'))  return 'Must be at least 8 characters';
    if (ctrl.hasError('weakPassword')) return 'Must include uppercase, lowercase, number, and symbol';
    return null;
  }

  onSubmit(): void {
    this.form.markAllAsTouched();
    if (this.form.invalid || this.loading()) return;

    this.loading.set(true);
    const { newPassword, confirmPassword } = this.form.value;

    this.authService
      .resetPassword({ resetToken: this.resetToken, newPassword, confirmPassword })
      .subscribe({
        next: () => {
          this.loading.set(false);
          this.toast.success('Password reset successfully! Please sign in.');
          this.router.navigate(['/auth/login']);
        },
        error: (err: ApiError) => {
          this.loading.set(false);
          this.toast.error(err.message);
        },
      });
  }
}
