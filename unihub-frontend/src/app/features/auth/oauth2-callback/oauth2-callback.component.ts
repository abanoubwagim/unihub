import { Component, inject, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../../core/services/auth.service';
import { ToastService } from '../../../core/services/toast.service';

@Component({
  selector: 'app-oauth2-callback',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './oauth2-callback.component.html',
  styleUrls: ['./oauth2-callback.component.scss'],
})
export class OAuth2CallbackComponent implements OnInit {
  private authService = inject(AuthService);
  private router = inject(Router);
  private toast = inject(ToastService);

  status: 'loading' | 'error' = 'loading';
  errorMessage = '';

  ngOnInit(): void {
    this.handleCallback();
  }

  private handleCallback(): void {
    const searchParams = new URLSearchParams(window.location.search);
    const errorParam = searchParams.get('error');

    if (errorParam) {
      this.handleError(errorParam);
      return;
    }

    // Parse one-time code from URL fragment (#code=...)
    const fragment = window.location.hash.substring(1); // remove '#'
    const fragmentParams = new URLSearchParams(fragment);
    const code = fragmentParams.get('code');

    if (!code) {
      this.handleError('MISSING_CODE');
      return;
    }

    this.authService.exchangeOAuth2Code(code).subscribe({
      next: () => {
        this.authService.getMe().subscribe({
          next: () => this.router.navigate(['/dashboard']),
          error: () => this.router.navigate(['/dashboard']),
        });
      },
      error: () => this.handleError('EXCHANGE_FAILED'),
    });
  }

  private handleError(code: string): void {
    this.status = 'error';
    const messages: Record<string, string> = {
      EMAIL_CONFLICT:   'This email is already linked to a different account.',
      ACCOUNT_DISABLED: 'Your account has been disabled. Please contact support.',
      OAUTH2_FAILED:    'Authentication failed. Please try again.',
      MISSING_CODE:     'Invalid callback URL. Please try again.',
      EXCHANGE_FAILED:  'Failed to authenticate. Please try again.',
      SERVER_ERROR:     'A server error occurred. Please try again.',
    };
    this.errorMessage = messages[code] ?? 'Authentication failed. Please try again.';
    this.toast.error(this.errorMessage);
  }

  retryLogin(): void {
    this.router.navigate(['/auth/login']);
  }
}
