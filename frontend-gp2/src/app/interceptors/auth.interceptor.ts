import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const router = inject(Router);
  const token = localStorage.getItem('token_ecommerce');
  // CLONAR LA SOLICITUD ORIGINAL Y AGREGAR EL TOKEN DE AUTENTICACIÓN SI EXISTE
  let authReq = req;
  if (token) {
    authReq = req.clone({
      setHeaders: {Authorization: `Bearer ${token}`}
    });
  }
  return next(authReq).pipe(
    // Manejar errores de autenticación
    catchError((error) => {
      if (error.status === 401 || error.status === 403) {
        localStorage.clear(); // Limpiar el almacenamiento local
        router.navigate(['/login']); // Redirigir al usuario a la página de inicio de sesión
      }
      return throwError(() => error); // Propagar el error para que pueda ser manejado por otros interceptores o componentes
    })
  );
};
