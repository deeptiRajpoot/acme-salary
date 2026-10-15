import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { catchError, throwError } from 'rxjs';
import { parseProblem } from './api-error';

/** Converts any failed HTTP response into a typed ApiError (RFC 7807 aware). */
export const errorInterceptor: HttpInterceptorFn = (req, next) =>
  next(req).pipe(
    catchError((err: unknown) =>
      throwError(() => (err instanceof HttpErrorResponse ? parseProblem(err.status, err.error, err.statusText) : err)),
    ),
  );
