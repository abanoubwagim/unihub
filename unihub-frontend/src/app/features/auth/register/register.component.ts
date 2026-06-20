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
import { ApiError, Role } from '../../../core/models/auth.models';

function passwordMatchValidator(group: AbstractControl): ValidationErrors | null {
  const pw  = group.get('password')?.value;
  const cpw = group.get('confirmPassword')?.value;
  return pw && cpw && pw !== cpw ? { passwordMismatch: true } : null;
}

function strongPassword(ctrl: AbstractControl): ValidationErrors | null {
  const v = ctrl.value as string;
  if (!v) return null;
  const ok = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])/.test(v);
  return ok ? null : { weakPassword: true };
}

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.scss'],
})
export class RegisterComponent {
  private fb = inject(FormBuilder);
  private authService = inject(AuthService);
  private router = inject(Router);
  private toast = inject(ToastService);

  readonly loading = signal(false);
  readonly showPassword = signal(false);
  readonly showConfirm = signal(false);

  readonly roles: { value: Role; label: string; emoji: string; desc: string }[] = [
    { value: 'STUDENT',    label: 'Student',    emoji: '🎓', desc: 'Find jobs & build your career' },
    { value: 'COMPANY',    label: 'Company',    emoji: '🏢', desc: 'Hire top talent from universities' },
    { value: 'UNIVERSITY', label: 'University', emoji: '🏛️', desc: 'Connect students with opportunities' },
  ];

  form: FormGroup = this.fb.group(
    {
      role:            ['STUDENT', Validators.required],
      email:           ['', [Validators.required, Validators.email]],
      password:        ['', [Validators.required, Validators.minLength(8), strongPassword]],
      confirmPassword: ['', Validators.required],
    },
    { validators: passwordMatchValidator }
  );

  get email()           { return this.form.get('email')!; }
  get password()        { return this.form.get('password')!; }
  get confirmPassword() { return this.form.get('confirmPassword')!; }
  get selectedRole()    { return this.form.get('role')!.value as Role; }

  fieldError(field: 'email' | 'password' | 'confirmPassword'): string | null {
    const ctrl = this.form.get(field)!;
    if (!ctrl.touched) return null;

    if (field === 'confirmPassword') {
      if (ctrl.hasError('required')) return 'Please confirm your password';
      if (this.form.hasError('passwordMismatch') && ctrl.touched) return 'Passwords do not match';
      return null;
    }

    if (ctrl.hasError('required'))   return 'This field is required';
    if (ctrl.hasError('email'))      return 'Enter a valid email address';
    if (ctrl.hasError('minlength'))  return 'Password must be at least 8 characters';
    if (ctrl.hasError('weakPassword')) return 'Must include uppercase, lowercase, number, and symbol';
    return null;
  }

  setRole(role: Role): void {
    this.form.patchValue({ role });
  }

  onSubmit(): void {
    this.form.markAllAsTouched();
    if (this.form.invalid || this.loading()) return;

    this.loading.set(true);

    this.authService.register(this.form.getRawValue()).subscribe({
      next: res => {
        this.loading.set(false);
        this.toast.success(`Account created! Please verify your email.`);
        this.router.navigate(['/auth/verify-email'], {
          queryParams: { email: res.email },
        });
      },
      error: (err: ApiError) => {
        this.loading.set(false);
        this.toast.error(err.message);
      },
    });
  }
}
