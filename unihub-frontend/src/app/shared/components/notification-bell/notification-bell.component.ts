import {
  Component,
  inject,
  HostListener,
  ElementRef,
  ChangeDetectionStrategy,
} from '@angular/core';
import { CommonModule} from '@angular/common';
import { NotificationService } from '../../../core/services/notification.service';
import type { NotificationItem, NotificationType } from '../../../core/models/notification.models';

// Icon

const TYPE_ICON: Record<NotificationType, string> = {
  JOB_APPLICATION_ACCEPTED:  '✅',
  JOB_APPLICATION_REJECTED:  '❌',
  CERTIFICATE_APPROVED:      '🎓',
  CERTIFICATE_REJECTED:      '📋',
  UNIVERSITY_LINKED:         '🏛️',
  JOB_APPLICATION_RECEIVED:  '📥',
  PARTNERSHIP_ACCEPTED:      '🤝',
  PARTNERSHIP_REJECTED:      '🤝',
  CERTIFICATE_SUBMITTED:     '📄',
  PARTNERSHIP_REQUESTED:     '🔔',
  CHAT_MESSAGE_RECEIVED:     '💬',
  WELCOME:                   '👋',
};

// Component

@Component({
  selector: 'app-notification-bell',
  standalone: true,
  imports: [CommonModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <!-- Bell Trigger  -->
    <button
      class="bell-btn"
      (click)="ns.togglePanel()"
      [attr.aria-expanded]="ns.panelOpen()"
      aria-label="Notifications"
    >
      <svg class="bell-btn__icon" viewBox="0 0 24 24" fill="none"
           xmlns="http://www.w3.org/2000/svg" aria-hidden="true">
        <path d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002
                 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6
                 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6
                 0v1a3 3 0 11-6 0v-1m6 0H9"
              stroke="currentColor" stroke-width="1.75"
              stroke-linecap="round" stroke-linejoin="round"/>
      </svg>

      @if (ns.badgeLabel(); as label) {
        <span class="bell-btn__badge" aria-live="polite">{{ label }}</span>
      }
    </button>

    <!--  Notification Panel  -->
    @if (ns.panelOpen()) {
      <div class="notif-panel" role="dialog" aria-label="Notifications panel">

        <!-- Header -->
        <div class="notif-panel__header">
          <span class="notif-panel__title">Notifications</span>
          <div class="notif-panel__header-actions">
            @if (ns.unreadCount() > 0) {
              <button
                class="notif-panel__mark-all"
                (click)="ns.markAllRead()"
                [disabled]="ns.loading()"
              >
                Mark all read
              </button>
            }
            <button
              class="notif-panel__refresh"
              (click)="ns.loadFirstPage()"
              [disabled]="ns.loading()"
              aria-label="Refresh notifications"
            >
              <svg viewBox="0 0 20 20" fill="currentColor"
                   [class.spinning]="ns.loading()" aria-hidden="true">
                <path fill-rule="evenodd"
                  d="M4 2a1 1 0 011 1v2.101a7.002 7.002 0 0111.601 2.566
                     1 1 0 11-1.885.666A5.002 5.002 0 005.999 7H9a1 1 0
                     010 2H4a1 1 0 01-1-1V3a1 1 0 011-1zm.008 9.057a1 1
                     0 011.276.61A5.002 5.002 0 0014.001 13H11a1 1 0
                     110-2h5a1 1 0 011 1v5a1 1 0 11-2 0v-2.101a7.002
                     7.002 0 01-11.601-2.566 1 1 0 01.61-1.276z"
                  clip-rule="evenodd"/>
              </svg>
            </button>
          </div>
        </div>

        <!-- Body -->
        <div class="notif-panel__body">

          <!-- Loading skeleton -->
          @if (ns.loading()) {
            <div class="notif-skeleton">
              @for (_ of [1,2,3]; track $index) {
                <div class="notif-skeleton__row">
                  <div class="notif-skeleton__icon"></div>
                  <div class="notif-skeleton__lines">
                    <div class="notif-skeleton__line notif-skeleton__line--title"></div>
                    <div class="notif-skeleton__line notif-skeleton__line--body"></div>
                  </div>
                </div>
              }
            </div>
          }

          <!-- Empty state -->
          @if (!ns.loading() && ns.notifications().length === 0) {
            <div class="notif-empty">
              <span class="notif-empty__icon" aria-hidden="true">🔔</span>
              <p class="notif-empty__title">All caught up!</p>
              <p class="notif-empty__sub">You have no notifications yet.</p>
            </div>
          }

          <!-- Notification list -->
          @if (!ns.loading() && ns.notifications().length > 0) {
            <ul class="notif-list" role="list">
              @for (n of ns.notifications(); track n.id) {
                <li class="notif-item" [class.notif-item--unread]="!n.read">
                  <span class="notif-item__icon" aria-hidden="true">
                    {{ iconFor(n.type) }}
                  </span>

                  <div class="notif-item__content">
                    <button
                      class="notif-item__text-btn"
                      (click)="onItemClick(n)"
                      [attr.aria-label]="n.title"
                    >
                      <span class="notif-item__title">{{ n.title }}</span>
                      <span class="notif-item__body">{{ n.body }}</span>
                      <span class="notif-item__time">{{ timeAgo(n.createdAt) }}</span>
                    </button>
                  </div>

                  @if (!n.read) {
                    <span class="notif-item__dot" aria-label="Unread"></span>
                  }

                  <button
                    class="notif-item__delete"
                    (click)="onDelete(n.id, $event)"
                    aria-label="Delete notification"
                  >
                    <svg viewBox="0 0 20 20" fill="currentColor" aria-hidden="true">
                      <path fill-rule="evenodd"
                        d="M4.293 4.293a1 1 0 011.414 0L10 8.586l4.293-4.293a1
                           1 0 111.414 1.414L11.414 10l4.293 4.293a1 1 0
                           01-1.414 1.414L10 11.414l-4.293 4.293a1 1 0
                           01-1.414-1.414L8.586 10 4.293 5.707a1 1 0
                           010-1.414z"
                        clip-rule="evenodd"/>
                    </svg>
                  </button>
                </li>
              }
            </ul>

            <!-- Load more -->
            @if (ns.hasMore()) {
              <div class="notif-load-more">
                <button
                  class="notif-load-more__btn"
                  (click)="ns.loadMore()"
                  [disabled]="ns.loadingMore()"
                >
                  @if (ns.loadingMore()) {
                    <span class="spinner" style="width:14px;height:14px;border-width:2px"></span>
                    Loading…
                  } @else {
                    Load more
                  }
                </button>
              </div>
            }
          }

        </div>
      </div>
    }
  `,
  styles: [`
    /* ── Host positioning (relative so the panel anchors to it) ── */
    :host {
      position: relative;
      display: inline-flex;
      align-items: center;
    }

    /*  Bell Button  */
    .bell-btn {
      position: relative;
      display: inline-flex;
      align-items: center;
      justify-content: center;
      width: 36px;
      height: 36px;
      border-radius: var(--radius-sm);
      color: var(--color-text-secondary);
      transition: color var(--transition-fast), background var(--transition-fast);
    }
    .bell-btn:hover {
      color: var(--color-text);
      background: var(--color-surface-alt);
    }
    .bell-btn[aria-expanded="true"] {
      color: var(--color-primary);
      background: var(--color-primary-subtle);
    }

    .bell-btn__icon {
      width: 20px;
      height: 20px;
      flex-shrink: 0;
    }

    /* Badge */
    .bell-btn__badge {
      position: absolute;
      top: 2px;
      right: 2px;
      min-width: 16px;
      height: 16px;
      padding: 0 3px;
      border-radius: var(--radius-full);
      background: var(--color-error);
      color: #fff;
      font-family: var(--font-body);
      font-size: 0.625rem;
      font-weight: 700;
      line-height: 16px;
      text-align: center;
      pointer-events: none;
      box-shadow: 0 0 0 2px var(--color-surface);
    }

    /*  Panel */
    .notif-panel {
      position: absolute;
      top: calc(100% + 8px);
      right: 0;
      width: 380px;
      max-height: 520px;
      background: var(--color-surface);
      border: 1px solid var(--color-border);
      border-radius: var(--radius-lg);
      box-shadow: var(--shadow-xl);
      display: flex;
      flex-direction: column;
      overflow: hidden;
      z-index: 1000;
      animation: panelIn 0.18s cubic-bezier(0.175, 0.885, 0.32, 1.1);
    }

    @keyframes panelIn {
      from { opacity: 0; transform: translateY(-8px) scale(0.97); }
      to   { opacity: 1; transform: translateY(0)   scale(1); }
    }

    /* Panel Header */
    .notif-panel__header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 0.875rem 1rem;
      border-bottom: 1px solid var(--color-border);
      flex-shrink: 0;
    }
    .notif-panel__title {
      font-family: var(--font-display);
      font-size: 0.9375rem;
      font-weight: 600;
      color: var(--color-text);
    }
    .notif-panel__header-actions {
      display: flex;
      align-items: center;
      gap: 0.5rem;
    }
    .notif-panel__mark-all {
      font-family: var(--font-body);
      font-size: 0.75rem;
      font-weight: 500;
      color: var(--color-primary);
      padding: 0.2rem 0.5rem;
      border-radius: var(--radius-sm);
      transition: background var(--transition-fast);
    }
    .notif-panel__mark-all:hover { background: var(--color-primary-subtle); }
    .notif-panel__mark-all:disabled { opacity: 0.5; cursor: default; }

    .notif-panel__refresh {
      display: inline-flex;
      align-items: center;
      justify-content: center;
      width: 26px;
      height: 26px;
      border-radius: var(--radius-sm);
      color: var(--color-text-muted);
      transition: color var(--transition-fast), background var(--transition-fast);
    }
    .notif-panel__refresh:hover { color: var(--color-text); background: var(--color-surface-alt); }
    .notif-panel__refresh svg {
      width: 14px;
      height: 14px;
    }
    .notif-panel__refresh svg.spinning {
      animation: spin 0.7s linear infinite;
    }

    /* Panel Body */
    .notif-panel__body {
      flex: 1;
      overflow-y: auto;
      overscroll-behavior: contain;
    }

    /* Scrollbar */
    .notif-panel__body::-webkit-scrollbar { width: 4px; }
    .notif-panel__body::-webkit-scrollbar-track { background: transparent; }
    .notif-panel__body::-webkit-scrollbar-thumb {
      background: var(--color-border);
      border-radius: 2px;
    }

    /*  Skeleton */
    .notif-skeleton { padding: 0.5rem 0; }
    .notif-skeleton__row {
      display: flex;
      align-items: flex-start;
      gap: 0.75rem;
      padding: 0.75rem 1rem;
    }
    .notif-skeleton__icon {
      width: 32px;
      height: 32px;
      border-radius: var(--radius-sm);
      background: var(--color-surface-alt);
      animation: pulse 1.4s ease-in-out infinite;
      flex-shrink: 0;
    }
    .notif-skeleton__lines {
      flex: 1;
      display: flex;
      flex-direction: column;
      gap: 0.4rem;
      padding-top: 0.2rem;
    }
    .notif-skeleton__line {
      height: 10px;
      border-radius: var(--radius-sm);
      background: var(--color-surface-alt);
      animation: pulse 1.4s ease-in-out infinite;
    }
    .notif-skeleton__line--title  { width: 55%; }
    .notif-skeleton__line--body   { width: 85%; }

    @keyframes pulse {
      0%, 100% { opacity: 1; }
      50%       { opacity: 0.45; }
    }

    /* Empty State */
    .notif-empty {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      gap: 0.4rem;
      padding: 3rem 1rem;
      text-align: center;
    }
    .notif-empty__icon { font-size: 2rem; opacity: 0.35; }
    .notif-empty__title {
      font-family: var(--font-display);
      font-size: 0.9375rem;
      font-weight: 600;
      color: var(--color-text);
    }
    .notif-empty__sub {
      font-size: 0.8125rem;
      color: var(--color-text-muted);
    }

    /*  Notification List  */
    .notif-list {
      list-style: none;
      padding: 0.375rem 0;
    }

    .notif-item {
      display: flex;
      align-items: flex-start;
      gap: 0.625rem;
      padding: 0.625rem 0.75rem 0.625rem 1rem;
      position: relative;
      transition: background var(--transition-fast);
    }
    .notif-item:hover { background: var(--color-surface-alt); }

    .notif-item--unread {
      background: color-mix(in srgb, var(--color-primary-subtle) 60%, transparent);
    }
    .notif-item--unread:hover {
      background: var(--color-primary-subtle);
    }

    /* Icon */
    .notif-item__icon {
      font-size: 1.125rem;
      line-height: 1;
      margin-top: 0.1rem;
      flex-shrink: 0;
      width: 24px;
      text-align: center;
    }

    /* Content (button for accessibility — entire row is clickable) */
    .notif-item__content { flex: 1; min-width: 0; }
    .notif-item__text-btn {
      display: block;
      width: 100%;
      text-align: left;
      background: none;
      border: none;
      cursor: pointer;
      padding: 0;
    }
    .notif-item__text-btn:focus-visible {
      outline: 2px solid var(--color-primary);
      border-radius: var(--radius-sm);
    }

    .notif-item__title {
      display: block;
      font-size: 0.8125rem;
      font-weight: 600;
      color: var(--color-text);
      line-height: 1.35;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }
    .notif-item__body {
      display: block;
      font-size: 0.75rem;
      color: var(--color-text-secondary);
      line-height: 1.4;
      margin-top: 0.125rem;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }
    .notif-item__time {
      display: block;
      font-size: 0.6875rem;
      color: var(--color-text-muted);
      margin-top: 0.25rem;
    }

    /* Unread dot */
    .notif-item__dot {
      width: 7px;
      height: 7px;
      border-radius: 50%;
      background: var(--color-primary);
      flex-shrink: 0;
      margin-top: 0.35rem;
    }

    /* Delete button */
    .notif-item__delete {
      display: none;
      align-items: center;
      justify-content: center;
      width: 24px;
      height: 24px;
      border-radius: var(--radius-sm);
      color: var(--color-text-muted);
      flex-shrink: 0;
      transition: color var(--transition-fast), background var(--transition-fast);
    }
    .notif-item:hover .notif-item__delete { display: inline-flex; }
    .notif-item__delete:hover {
      color: var(--color-error);
      background: var(--color-error-bg);
    }
    .notif-item__delete svg {
      width: 14px;
      height: 14px;
    }

    /*  Load More  */
    .notif-load-more {
      padding: 0.625rem 1rem;
      border-top: 1px solid var(--color-border);
    }
    .notif-load-more__btn {
      width: 100%;
      display: inline-flex;
      align-items: center;
      justify-content: center;
      gap: 0.375rem;
      padding: 0.5rem;
      border-radius: var(--radius-sm);
      font-family: var(--font-body);
      font-size: 0.8125rem;
      font-weight: 500;
      color: var(--color-primary);
      transition: background var(--transition-fast);
    }
    .notif-load-more__btn:hover { background: var(--color-primary-subtle); }
    .notif-load-more__btn:disabled { opacity: 0.6; cursor: default; }
  `],
})
export class NotificationBellComponent {
  protected ns = inject(NotificationService);
  private elRef = inject(ElementRef);

  //  Close panel on click outside
  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent): void {
    if (
      this.ns.panelOpen() &&
      !this.elRef.nativeElement.contains(event.target)
    ) {
      this.ns.closePanel();
    }
  }

  //  Close panel on Escape
  @HostListener('document:keydown.escape')
  onEscape(): void {
    this.ns.closePanel();
  }

  //  Helpers

  iconFor(type: NotificationType): string {
    return TYPE_ICON[type] ?? '🔔';
  }

  onItemClick(notification: NotificationItem): void {
    if (!notification.read) {
      this.ns.markRead(notification.id);
    }
    // Future: navigate based on referenceType / referenceId
  }

  onDelete(id: string, event: MouseEvent): void {
    event.stopPropagation();
    this.ns.deleteNotification(id);
  }

  // Convert ISO-8601 string → friendly relative label.
  timeAgo(isoString: string): string {
    const now = Date.now();
    const then = new Date(isoString).getTime();
    const diffMs = now - then;
    const diffMin = Math.floor(diffMs / 60_000);

    if (diffMin < 1)    return 'Just now';
    if (diffMin < 60)   return `${diffMin}m ago`;

    const diffH = Math.floor(diffMin / 60);
    if (diffH < 24)     return `${diffH}h ago`;

    const diffD = Math.floor(diffH / 24);
    if (diffD < 7)      return `${diffD}d ago`;

    return new Date(isoString).toLocaleDateString(undefined, {
      month: 'short',
      day: 'numeric',
    });
  }
}
