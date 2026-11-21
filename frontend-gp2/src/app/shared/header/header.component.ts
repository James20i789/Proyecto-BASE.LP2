import { Component, inject, OnInit, signal } from '@angular/core';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { finalize } from 'rxjs';
import { AuthService } from '../../core/services/auth.service';
import { CartService } from '../../core/services/cart.service';
import { AlertService } from '../../core/services/alert.service';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [RouterLink, RouterLinkActive],
  templateUrl: './header.component.html',
  styleUrl: './header.component.css'
})
export class HeaderComponent implements OnInit {
  readonly cart = inject(CartService);
  readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly alerts = inject(AlertService);
  readonly signingOut = signal(false);

  ngOnInit(): void {
    this.auth.session().subscribe();
  }

  logout(): void {
    if (this.signingOut()) return;
    this.signingOut.set(true);
    this.auth.logout().pipe(finalize(() => this.signingOut.set(false))).subscribe({
      next: () => this.router.navigateByUrl('/home'),
      error: () => this.alerts.toast('No se pudo cerrar la sesión. Inténtalo otra vez.', 'error')
    });
  }
}
