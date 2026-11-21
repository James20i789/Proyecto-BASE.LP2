import { HttpClient } from '@angular/common/http';
import { inject, Injectable, signal } from '@angular/core';
import { catchError, Observable, of, switchMap, tap } from 'rxjs';
import { API } from '../api';
import { Account, Registration } from '../Interfaces/auth.interface';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly url = `${API}/auth`;
  private readonly account = signal<Account | null>(null);
  readonly user = this.account.asReadonly();

  constructor() {
    for (const key of ['token_ecommerce', 'user_id_persona', 'user_role', 'user_name']) {
      localStorage.removeItem(key);
    }
  }

  session(): Observable<Account | null> {
    return this.http.get<Account | null>(`${this.url}/session`).pipe(
      tap(account => this.account.set(account)),
      catchError(() => of(this.account()))
    );
  }

  login(usuario: string, password: string): Observable<Account> {
    return this.csrf().pipe(
      switchMap(() => this.http.post<Account>(`${this.url}/login`, { usuario, password })),
      tap(account => this.account.set(account))
    );
  }

  register(data: Registration): Observable<unknown> {
    return this.csrf().pipe(switchMap(() => this.http.post(`${this.url}/registro`, data)));
  }

  logout(): Observable<void> {
    return this.csrf().pipe(
      switchMap(() => this.http.post<void>(`${this.url}/logout`, null)),
      tap(() => this.expire())
    );
  }

  expire(): void {
    this.account.set(null);
  }

  private csrf(): Observable<void> {
    return this.http.get<void>(`${this.url}/csrf`);
  }
}
