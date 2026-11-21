import { CurrencyPipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { finalize } from 'rxjs';
import { AuthService } from '../../core/services/auth.service';
import { CartService } from '../../core/services/cart.service';
import { PedidoService } from '../../core/services/pedido.service';

@Component({
  selector: 'app-carrito',
  standalone: true,
  imports: [CurrencyPipe, RouterLink],
  templateUrl: './carrito.component.html',
  styleUrl: './carrito.component.css'
})
export class CarritoComponent {
  readonly cart = inject(CartService);
  readonly auth = inject(AuthService);
  private readonly orders = inject(PedidoService);
  private readonly router = inject(Router);
  readonly busy = signal(false);
  readonly error = signal('');

  pagar(): void {
    const user = this.auth.user();
    if (!user) {
      this.router.navigate(['/login'], { queryParams: { volver: '/carrito' } });
      return;
    }
    if (this.busy() || !this.cart.totalItems() || user.rol !== 'CLIENTE') return;
    this.error.set('');
    const { key, items } = this.cart.checkout(user.idPersona);
    this.busy.set(true);
    this.orders.crearPedido({ intento: key, items }).pipe(finalize(() => this.busy.set(false))).subscribe({
      next: order => {
        this.cart.rememberOrder(key, order.idPedido);
        if (order.checkoutUrl) {
          window.location.assign(order.checkoutUrl);
        } else {
          this.router.navigate(['/pedido', order.idPedido]);
        }
      },
      error: (error: HttpErrorResponse) => {
        if (error.status === 401) {
          this.router.navigate(['/login'], { queryParams: { volver: '/carrito' } });
        } else {
          this.error.set(error.error?.message || 'No pudimos abrir el pago. Inténtalo de nuevo.');
        }
      }
    });
  }
}
