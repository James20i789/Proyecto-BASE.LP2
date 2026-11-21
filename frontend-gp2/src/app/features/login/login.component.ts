import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { finalize } from 'rxjs';
import { accountReturnPath } from '../../core/account.guard';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [FormsModule, RouterLink],
  templateUrl: './login.component.html'
})
export class LoginComponent {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  readonly returnPath = accountReturnPath(this.route.snapshot.queryParamMap.get('volver'));
  readonly registered = this.route.snapshot.queryParamMap.get('registro') === 'ok';
  readonly pending = signal(false);
  readonly visible = signal(false);
  readonly error = signal('');
  username = '';
  password = '';

  submit(): void {
    if (this.pending()) return;
    this.pending.set(true);
    this.error.set('');
    this.auth.login(this.username.trim(), this.password)
      .pipe(finalize(() => this.pending.set(false))).subscribe({
        next: user => {
          this.password = '';
          this.router.navigateByUrl(user.rol === 'ADMIN' && this.returnPath === '/home' ? '/productos' : this.returnPath,
            { replaceUrl: true });
        },
        error: (error: HttpErrorResponse) => {
          this.error.set(error.status === 401 ? 'Usuario o contraseña incorrectos.' :
            error.error?.message || 'No pudimos iniciar sesión. Inténtalo de nuevo.');
        }
      });
  }
}
