import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { TokenService } from '../services/token.service';

export const jwtInterceptor: HttpInterceptorFn = (req, next) => {
  const tokenService = inject(TokenService);
  const token = tokenService.getAccessToken();

  // Skip attaching token for refresh (cookie-based) and public endpoints
  const skipUrls = ['/auth/refresh', '/auth/login', '/auth/register'];
  const shouldSkip = skipUrls.some(url => req.url.includes(url));

  if (token && !shouldSkip) {
    return next(
      req.clone({ setHeaders: { Authorization: `Bearer ${token}` } })
    );
  }

  return next(req);
};
