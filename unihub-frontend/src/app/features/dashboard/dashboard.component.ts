import { Component, inject, signal, OnInit, OnDestroy } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../core/services/auth.service';
import { ToastService } from '../../core/services/toast.service';
import { NotificationService } from '../../core/services/notification.service';
import { NotificationBellComponent } from '../../shared/components/notification-bell/notification-bell.component';
import { UserResponse, ApiError } from '../../core/models/auth.models';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink, NotificationBellComponent],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss'],
})
export class DashboardComponent implements OnInit, OnDestroy {
  private authService   = inject(AuthService);
  private router        = inject(Router);
  private toast         = inject(ToastService);
  private notifService  = inject(NotificationService);

  readonly user         = signal<UserResponse | null>(null);
  readonly loadingUser  = signal(true);
  readonly loggingOut   = signal(false);

  ngOnInit(): void {
    // Fetch fresh user data
    this.authService.getMe().subscribe({
      next: u => {
        this.user.set(u);
        this.loadingUser.set(false);
        //  Start notification polling + WebSocket once we have the user
        this.notifService.startPolling();
        this.notifService.connectWebSocket();
      },
      error: () => {
        this.loadingUser.set(false);
        // authGuard + error interceptor handle redirect
      },
    });
  }

  ngOnDestroy(): void {
    // Cleanup when leaving the dashboard (navigating away)
    this.notifService.stopPolling();
    this.notifService.disconnectWebSocket();
  }

  // Presentation helpers

  get roleLabel(): string {
    const map: Record<string, string> = {
      STUDENT:    'Student',
      COMPANY:    'Company',
      UNIVERSITY: 'University',
      ADMIN:      'Administrator',
    };
    return map[this.user()?.role ?? ''] ?? '';
  }

  get roleEmoji(): string {
    const map: Record<string, string> = {
      STUDENT:    '🎓',
      COMPANY:    '🏢',
      UNIVERSITY: '🏛️',
      ADMIN:      '⚙️',
    };
    return map[this.user()?.role ?? ''] ?? '👤';
  }

  get statusColor(): string {
    const map: Record<string, string> = {
      ACTIVE:    'status--active',
      PENDING:   'status--pending',
      SUSPENDED: 'status--warning',
      BANNED:    'status--error',
    };
    return map[this.user()?.status ?? ''] ?? '';
  }

  // Actions

  logout(): void {
    if (this.loggingOut()) return;
    this.loggingOut.set(true);

    this.authService.logout().subscribe({
      next: () => {
        this.notifService.stopPolling();
        this.notifService.disconnectWebSocket();
        this.loggingOut.set(false);
        this.toast.success('Signed out successfully.');
        this.router.navigate(['/auth/login']);
      },
      error: () => {
        this.authService.clearSession();
        this.notifService.stopPolling();
        this.notifService.disconnectWebSocket();
        this.loggingOut.set(false);
        this.router.navigate(['/auth/login']);
      },
    });
  }
}
