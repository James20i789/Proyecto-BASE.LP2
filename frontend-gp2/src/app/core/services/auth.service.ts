import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { BehaviorSubject, catchError, map, Observable, of, tap } from 'rxjs';
import { ILoginResponse } from '../Interfaces/auth.interface';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private http = inject(HttpClient);
  private router = inject(Router);
  
  // API URL for authentication endpoints
  private apiUrl = '/api/v1/auth'; // RUTA DEL PROXY
  // BehaviorSubject to track login status
  private loggedIn = new BehaviorSubject<boolean>(!!localStorage.getItem('token_ecommerce'));
  // Observable to track login status
  isLoggedIn$ = this.loggedIn.asObservable();

  // LOGIN - PROCESO DE AUTENTICACIÓN
  login(credentials: { usuario: string; password: string }): Observable<boolean> {
    return this.http.post<ILoginResponse>(`${this.apiUrl}/login`, credentials).pipe (
      tap(reponse => {
        if (reponse && reponse.token) {
          // Store the token and user information in localStorage
          localStorage.setItem('token_ecommerce', reponse.token);
          localStorage.setItem('user_id_persona', reponse.idPersona.toString());
          localStorage.setItem('user_role', reponse.rol);
          localStorage.setItem('user_name', reponse.usuario);
          // Update the loggedIn BehaviorSubject to true
          this.loggedIn.next(true);
          this.router.navigate(['/home']);
        }
  }),
  // Map the response to a boolean indicating success
  map(response => !!response.token),
  catchError(() => {
    this.logout(); // Clear any existing session data on error
    return of(false);
  })
);
}
// LOGOUT - PROCESO DE CIERRE DE SESIÓN
logout(): void {
  localStorage.clear();
  this.loggedIn.next(false);
  this.router.navigate(['/login']);
}
// Get the token from localStorage
getToken(): string | null {
  return localStorage.getItem('token_ecommerce');
}
// REGISTRO - PROCESO DE REGISTRO DE USUARIO
registrar(datos:any): Observable<any> {
  return this.http.post(`${this.apiUrl}/registro`, datos);
}
}
