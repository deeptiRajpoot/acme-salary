import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { AuthService } from './auth.service';

const PUBLIC_ENDPOINTS = ['/api/auth/signin', '/api/auth/signup'];

/** Adds the bearer token to API calls and signs the user out when the server says it is no longer valid. */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService);
  const token = auth.token();
  if (!token || !req.url.startsWith('/api/') || PUBLIC_ENDPOINTS.includes(req.url)) return next(req);
  return next(req.clone({ setHeaders: { Authorization: `Bearer ${token}` } })).pipe(
    catchError((err: unknown) => {
      if (err instanceof HttpErrorResponse && err.status === 401) auth.expire();
      return throwError(() => err);
    }),
  );
};
