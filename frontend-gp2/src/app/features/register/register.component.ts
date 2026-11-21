import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { finalize } from 'rxjs';
import { accountReturnPath } from '../../core/account.guard';
import { Registration } from '../../core/Interfaces/auth.interface';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [FormsModule, RouterLink],
  templateUrl: './register.component.html'
})
export class RegisterComponent {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  readonly returnPath = accountReturnPath(inject(ActivatedRoute).snapshot.queryParamMap.get('volver'));
  readonly pending = signal(false);
  readonly error = signal('');
  readonly fields = signal<Record<string, string>>({});
  readonly form: Registration = { nombre: '', usuario: '', email: '', telefono: '', direccion: '', password: '' };

  submit(): void {
    if (this.pending()) return;
    this.pending.set(true);
    this.error.set('');
    this.fields.set({});
    this.auth.register(this.form).pipe(finalize(() => this.pending.set(false))).subscribe({
      next: () => this.router.navigate(['/login'], {
        queryParams: { registro: 'ok', volver: this.returnPath }, replaceUrl: true
      }),
      error: (error: HttpErrorResponse) => {
        this.fields.set(error.error?.errors ?? {});
        this.error.set(error.error?.message || 'No pudimos crear tu cuenta.');
      }
    });
  }
}
