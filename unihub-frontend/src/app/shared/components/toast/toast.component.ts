import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ToastService, Toast } from '../../../core/services/toast.service';

@Component({
  selector: 'app-toast',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="toast-container">
      @for (toast of toastService.toasts(); track toast.id) {
        <div
          class="toast toast--{{ toast.type }}"
          role="alert"
          aria-live="assertive"
        >
          <span class="toast__icon">{{ iconFor(toast) }}</span>
          <span class="toast__message">{{ toast.message }}</span>
          <button
            class="toast__close"
            (click)="toastService.dismiss(toast.id)"
            aria-label="Dismiss"
          >✕</button>
        </div>
      }
    </div>
  `,
  styles: [`
    .toast-container {
      position: fixed;
      top: 1.5rem;
      right: 1.5rem;
      z-index: 9999;
      display: flex;
      flex-direction: column;
      gap: 0.75rem;
      max-width: 420px;
      pointer-events: none;
    }

    .toast {
      display: flex;
      align-items: center;
      gap: 0.75rem;
      padding: 1rem 1.25rem;
      border-radius: 12px;
      font-family: var(--font-body);
      font-size: 0.875rem;
      font-weight: 500;
      box-shadow: 0 8px 32px rgba(0, 0, 0, 0.15), 0 2px 8px rgba(0, 0, 0, 0.08);
      pointer-events: all;
      animation: toastSlideIn 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275);
      border: 1px solid transparent;
    }

    .toast--success { background: #ECFDF5; color: #065F46; border-color: #A7F3D0; }
    .toast--error   { background: #FEF2F2; color: #991B1B; border-color: #FECACA; }
    .toast--warning { background: #FFFBEB; color: #92400E; border-color: #FDE68A; }
    .toast--info    { background: #EFF6FF; color: #1E40AF; border-color: #BFDBFE; }

    .toast__icon    { font-size: 1rem; flex-shrink: 0; }
    .toast__message { flex: 1; line-height: 1.4; }

    .toast__close {
      background: none;
      border: none;
      cursor: pointer;
      font-size: 0.75rem;
      opacity: 0.5;
      padding: 0.25rem;
      border-radius: 4px;
      transition: opacity 0.2s;
      color: inherit;
      flex-shrink: 0;
    }
    .toast__close:hover { opacity: 1; }

    @keyframes toastSlideIn {
      from { opacity: 0; transform: translateX(100%); }
      to   { opacity: 1; transform: translateX(0); }
    }
  `]
})
export class ToastComponent {
  protected toastService = inject(ToastService);

  iconFor(toast: Toast): string {
    const icons: Record<string, string> = {
      success: '✓',
      error: '✕',
      warning: '⚠',
      info: 'ℹ',
    };
    return icons[toast.type] ?? 'ℹ';
  }
}
