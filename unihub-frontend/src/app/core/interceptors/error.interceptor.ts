import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { TokenService } from '../services/token.service';
import { ApiError } from '../models/auth.models';

export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const router = inject(Router);
  const tokenService = inject(TokenService);

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      // Session expired — redirect to login
      if (error.status === 401 && !req.url.includes('/auth/')) {
        tokenService.clearAccessToken();
        router.navigate(['/auth/login']);
      }

      const apiError: ApiError = {
        status: error.status,
        message:
          error.error?.error ??
          error.error?.message ??
          error.message ??
          'Something went wrong',
      };

      return throwError(() => apiError);
    })
  );
};
