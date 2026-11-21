import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { API } from '../core/api';
import { AuthService } from '../core/services/auth.service';

export const authInterceptor: HttpInterceptorFn = (request, next) => {
  if (!request.url.startsWith(`${API}/`) || request.url.startsWith(`${API}/auth/`)) {
    return next(request);
  }
  const auth = inject(AuthService);
  return next(request).pipe(catchError((error: HttpErrorResponse) => {
    if (error.status === 401) {
      auth.expire();
    }
    return throwError(() => error);
  }));
};
