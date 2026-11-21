import { CurrencyPipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, DestroyRef, inject, OnInit, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { catchError, EMPTY, exhaustMap, filter, finalize, merge, Subject, switchMap, take, timer } from 'rxjs';
import { ESTADO_PEDIDO, Pedido } from '../../core/Interfaces/pedido.interface';
import { AuthService } from '../../core/services/auth.service';
import { CartService } from '../../core/services/cart.service';
import { PedidoService } from '../../core/services/pedido.service';

@Component({
  selector: 'app-pedido',
  standalone: true,
  imports: [CurrencyPipe, RouterLink],
  templateUrl: './pedido.component.html',
  styleUrl: './pedidos.component.css'
})
export class PedidoComponent implements OnInit {
  private readonly orders = inject(PedidoService);
  private readonly cart = inject(CartService);
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly destroyRef = inject(DestroyRef);
  private readonly refresh = new Subject<void>();
  readonly order = signal<Pedido | null>(null);
  readonly busy = signal(false);
  readonly error = signal('');
  readonly labels = ESTADO_PEDIDO;

  ngOnInit(): void {
    this.route.paramMap.pipe(
      switchMap(params => {
        const id = Number(params.get('id'));
        this.order.set(null);
        this.error.set('');
        if (!Number.isSafeInteger(id) || id < 1) {
          this.error.set('Pedido no encontrado.');
          return EMPTY;
        }
        return merge(timer(0, 5000).pipe(take(25), filter(() => {
          const order = this.order();
          return !order || ['PENDIENTE', 'REEMBOLSO_PENDIENTE'].includes(order.estado);
        })), this.refresh).pipe(exhaustMap(() => this.fetch(id)));
      }),
      takeUntilDestroyed(this.destroyRef)
    ).subscribe(order => {
      this.error.set('');
      this.order.set(order);
      const owner = this.auth.user()?.idPersona;
      if (owner && order.estado !== 'PENDIENTE') {
        this.cart.finishOrder(owner, order.idPedido, order.estado === 'PAGADO');
      }
    });
  }

  reload(): void {
    this.refresh.next();
  }

  description(order: Pedido): string {
    switch (order.estado) {
      case 'PAGADO': return 'Mercado Pago confirmó el cobro de este pedido.';
      case 'VENCIDO': return 'La reserva venció. Puedes volver al carrito para empezar otro pedido.';
      case 'REEMBOLSO_PENDIENTE': return 'Mercado Pago está procesando la devolución.';
      case 'REEMBOLSADO': return 'Mercado Pago registró la devolución del cobro.';
      case 'REEMBOLSO_PARCIAL': return 'Mercado Pago registró una devolución parcial.';
      case 'CONTRACARGO': return 'Mercado Pago registró una disputa sobre este cobro.';
      case 'PENDIENTE':
        if (!order.venceEn) return 'Este pedido se registró antes de habilitar el pago en línea.';
        if (order.estadoPago === 'SIN_PREFERENCIA') return 'El pago no llegó a abrirse. Puedes reintentar desde el carrito.';
        if (order.estadoPago === 'rejected' || order.estadoPago === 'cancelled') {
          return 'El pago anterior no se completó. Si la reserva sigue disponible, puedes volver a intentarlo.';
        }
        return 'Estamos esperando la confirmación de Mercado Pago. Esta página se actualiza automáticamente durante dos minutos.';
    }
  }

  private fetch(id: number) {
    this.busy.set(true);
    return this.orders.get(id).pipe(
      catchError((error: HttpErrorResponse) => {
        if (error.status === 401) {
          this.router.navigate(['/login'], { queryParams: { volver: '/pedido/' + id } });
        }
        this.error.set(error.status === 404 ? 'Pedido no encontrado.' : 'No pudimos consultar este pedido.');
        return EMPTY;
      }),
      finalize(() => this.busy.set(false))
    );
  }
}
