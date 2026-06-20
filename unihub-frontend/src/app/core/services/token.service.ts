import { Injectable, signal, computed } from '@angular/core';

const ACCESS_TOKEN_KEY = 'uh_access_token';

@Injectable({ providedIn: 'root' })
export class TokenService {
  private _token = signal<string | null>(
    typeof window !== 'undefined' ? localStorage.getItem(ACCESS_TOKEN_KEY) : null
  );

  readonly hasToken = computed(() => !!this._token());

  setAccessToken(token: string): void {
    localStorage.setItem(ACCESS_TOKEN_KEY, token);
    this._token.set(token);
  }

  getAccessToken(): string | null {
    return this._token();
  }

  clearAccessToken(): void {
    localStorage.removeItem(ACCESS_TOKEN_KEY);
    this._token.set(null);
  }

  isTokenPresent(): boolean {
    return this.hasToken();
  }
}
