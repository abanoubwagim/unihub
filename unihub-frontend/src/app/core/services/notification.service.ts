import {
  Injectable,
  inject,
  signal,
  computed,
  OnDestroy,
} from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { interval, Subscription } from 'rxjs';
import { environment } from '../../../environments/environment';
import type {
  NotificationItem,
  PageResponse,
  NotificationPreference,
  UpdatePreferencesRequest,
} from '../models/notification.models';
import { TokenService } from './token.service';
import { ToastService } from './toast.service';

// Constants

const BASE = `${environment.apiUrl}/notifications`;
const WS_URL = `${environment.backendUrl}/ws`;
const POLL_INTERVAL_MS = 30_000;   // 30 s — background unread-count refresh
const PAGE_SIZE = 20;

// Service

@Injectable({ providedIn: 'root' })
export class NotificationService implements OnDestroy {

  // DI
  private readonly http          = inject(HttpClient);
  private readonly tokenService  = inject(TokenService);
  private readonly toast         = inject(ToastService);

  // State Signals
  readonly notifications  = signal<NotificationItem[]>([]);
  readonly unreadCount    = signal<number>(0);
  readonly totalElements  = signal<number>(0);
  readonly currentPage    = signal<number>(0);
  readonly isLast         = signal<boolean>(true);
  readonly loading        = signal<boolean>(false);
  readonly loadingMore    = signal<boolean>(false);
  readonly panelOpen      = signal<boolean>(false);

  // Derived
  readonly hasMore = computed(() => !this.isLast());
  readonly badgeLabel = computed(() => {
    const n = this.unreadCount();
    return n > 99 ? '99+' : n > 0 ? String(n) : null;
  });

  // Internal
  private pollSub: Subscription | null = null;
  private stompClient: unknown = null;   // @stomp/stompjs Client

  // Lifecycle
  ngOnDestroy(): void {
    this.stopPolling();
    this.disconnectWebSocket();
  }

  // Polling  (background unread-count refresh)
  startPolling(): void {
    if (this.pollSub) return;          // already running
    this.refreshUnreadCount();         // immediate first tick
    this.pollSub = interval(POLL_INTERVAL_MS).subscribe(() => {
      this.refreshUnreadCount();
    });
  }

  stopPolling(): void {
    this.pollSub?.unsubscribe();
    this.pollSub = null;
  }

  private refreshUnreadCount(): void {
    if (!this.tokenService.isTokenPresent()) return;
    this.http
      .get<{ unreadCount: number }>(`${BASE}/unread-count`)
      .subscribe({
        next: res => this.unreadCount.set(res.unreadCount),
        error: () => { /* silent — don't spam toasts for background polls */ },
      });
  }

    // WebSocket  (real-time push)
  async connectWebSocket(): Promise<void> {
    if (this.stompClient) return;      // already connected
    const token = this.tokenService.getAccessToken();
    if (!token) return;

    try {
      // Dynamic imports — keeps the service compilable even if packages are
      // absent; the catch block handles that case transparently.
      const [{ Client }, { default: SockJS }] = await Promise.all([
        import('@stomp/stompjs') as Promise<any>,
        import('sockjs-client') as Promise<any>,
      ]);

      const client = new Client({
        webSocketFactory: () => new SockJS(WS_URL),
        connectHeaders: { Authorization: `Bearer ${token}` },
        reconnectDelay: 5000,
        onConnect: () => {
          // Subscribe to the user-specific queue that the backend targets via
          // SimpMessagingTemplate.convertAndSendToUser(userId, "/queue/notifications", …)
          client.subscribe('/user/queue/notifications', (frame: any) => {
            try {
              const notification = JSON.parse(frame.body) as NotificationItem;
              this.handleRealTimePush(notification);
            } catch {
              // malformed frame — ignore
            }
          });
        },
        onStompError: () => {
          // WebSocket auth failure / server error — polling takes over
        },
        onDisconnect: () => {
          this.stompClient = null;
        },
      });

      client.activate();
      this.stompClient = client;
    } catch {
      console.debug('[NotificationService] WebSocket unavailable; using polling only.');
    }
  }

  disconnectWebSocket(): void {
    if (this.stompClient) {
      (this.stompClient as any).deactivate?.();
      this.stompClient = null;
    }
  }


  private handleRealTimePush(notification: NotificationItem): void {
    // Deduplicate (server may echo on reconnect)
    const alreadyExists = this.notifications().some(n => n.id === notification.id);
    if (!alreadyExists) {
      this.notifications.update(list => [notification, ...list]);
    }
    if (!notification.read) {
      this.unreadCount.update(c => c + 1);
    }
  }

 // Panel  (open / close)
  togglePanel(): void {
    const next = !this.panelOpen();
    this.panelOpen.set(next);
    if (next && this.notifications().length === 0) {
      // First open — load the first page
      this.loadFirstPage();
    }
  }

  closePanel(): void {
    this.panelOpen.set(false);
  }

  // Fetch
  loadFirstPage(): void {
    if (this.loading()) return;
    this.loading.set(true);
    this.currentPage.set(0);

    this.http
      .get<PageResponse<NotificationItem>>(BASE, {
        params: { page: 0, size: PAGE_SIZE },
      })
      .subscribe({
        next: res => {
          this.notifications.set(res.content);
          this.totalElements.set(res.totalElements);
          this.isLast.set(res.last);
          this.currentPage.set(0);
          // Sync unread count from fresh data
          const unread = res.content.filter(n => !n.read).length;
          // Only bump unreadCount down — don't overwrite with stale partial page
          // A separate /unread-count request is authoritative.
          this.loading.set(false);
        },
        error: () => {
          this.loading.set(false);
          this.toast.error('Could not load notifications. Please try again.');
        },
      });
  }

  /** Append the next page. */
  loadMore(): void {
    if (this.loadingMore() || this.isLast()) return;
    const nextPage = this.currentPage() + 1;
    this.loadingMore.set(true);

    this.http
      .get<PageResponse<NotificationItem>>(BASE, {
        params: { page: nextPage, size: PAGE_SIZE },
      })
      .subscribe({
        next: res => {
          this.notifications.update(list => [...list, ...res.content]);
          this.totalElements.set(res.totalElements);
          this.isLast.set(res.last);
          this.currentPage.set(nextPage);
          this.loadingMore.set(false);
        },
        error: () => {
          this.loadingMore.set(false);
          this.toast.error('Could not load more notifications.');
        },
      });
  }

 // Mutations
  markRead(notificationId: string): void {
    // Optimistic update
    this.notifications.update(list =>
      list.map(n =>
        n.id === notificationId
          ? { ...n, read: true, readAt: new Date().toISOString() }
          : n
      )
    );
    this.unreadCount.update(c => Math.max(0, c - 1));

    this.http
      .put<NotificationItem>(`${BASE}/${notificationId}/read`, {})
      .subscribe({
        // Sync server response (authoritative timestamps)
        next: updated => {
          this.notifications.update(list =>
            list.map(n => (n.id === updated.id ? updated : n))
          );
        },
        error: () => {
          // Rollback optimistic update
          this.notifications.update(list =>
            list.map(n =>
              n.id === notificationId
                ? { ...n, read: false, readAt: null }
                : n
            )
          );
          this.unreadCount.update(c => c + 1);
        },
      });
  }

  markAllRead(): void {
    const previousUnread = this.unreadCount();
    // Optimistic update
    this.notifications.update(list =>
      list.map(n => ({ ...n, read: true, readAt: n.readAt ?? new Date().toISOString() }))
    );
    this.unreadCount.set(0);

    this.http.put<void>(`${BASE}/read-all`, {}).subscribe({
      error: () => {
        // Rollback
        this.notifications.update(list =>
          list.map(n => ({ ...n, read: n.readAt != null, readAt: n.readAt }))
        );
        this.unreadCount.set(previousUnread);
        this.toast.error('Could not mark all notifications as read.');
      },
    });
  }

  deleteNotification(notificationId: string): void {
    const removed = this.notifications().find(n => n.id === notificationId);
    // Optimistic update
    this.notifications.update(list => list.filter(n => n.id !== notificationId));
    this.totalElements.update(t => Math.max(0, t - 1));
    if (removed && !removed.read) {
      this.unreadCount.update(c => Math.max(0, c - 1));
    }

    this.http.delete<void>(`${BASE}/${notificationId}`).subscribe({
      error: () => {
        // Rollback
        if (removed) {
          this.notifications.update(list => [removed, ...list]);
          this.totalElements.update(t => t + 1);
          if (!removed.read) this.unreadCount.update(c => c + 1);
        }
        this.toast.error('Could not delete notification.');
      },
    });
  }

  // Preferences  (kept here for completeness — wire to a settings page later)
  getPreferences() {
    return this.http.get<NotificationPreference[]>(`${BASE}/preferences`);
  }

  updatePreferences(request: UpdatePreferencesRequest) {
    return this.http.put<NotificationPreference[]>(`${BASE}/preferences`, request);
  }
}
