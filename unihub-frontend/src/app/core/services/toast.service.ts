import { Injectable, signal } from '@angular/core';

export type ToastType = 'success' | 'error' | 'warning' | 'info';

export interface Toast {
  id: string;
  message: string;
  type: ToastType;
}

@Injectable({ providedIn: 'root' })
export class ToastService {
  readonly toasts = signal<Toast[]>([]);

  success(message: string, duration = 4000): void {
    this.push(message, 'success', duration);
  }

  error(message: string, duration = 6000): void {
    this.push(message, 'error', duration);
  }

  warning(message: string, duration = 5000): void {
    this.push(message, 'warning', duration);
  }

  info(message: string, duration = 4000): void {
    this.push(message, 'info', duration);
  }

  dismiss(id: string): void {
    this.toasts.update(ts => ts.filter(t => t.id !== id));
  }

  private push(message: string, type: ToastType, duration: number): void {
    const id = crypto.randomUUID();
    this.toasts.update(ts => [...ts, { id, message, type }]);
    setTimeout(() => this.dismiss(id), duration);
  }
}
