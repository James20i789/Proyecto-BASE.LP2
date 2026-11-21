import { CurrencyPipe, DatePipe } from '@angular/common';
import { Component, inject, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { ESTADO_PEDIDO, Pedido } from '../../core/Interfaces/pedido.interface';
import { PedidoService } from '../../core/services/pedido.service';

@Component({
  selector: 'app-pedidos',
  standalone: true,
  imports: [CurrencyPipe, DatePipe, RouterLink],
  templateUrl: './pedidos.component.html',
  styleUrl: './pedidos.component.css'
})
export class PedidosComponent implements OnInit {
  private readonly orders = inject(PedidoService);
  readonly labels = ESTADO_PEDIDO;
  readonly data = signal<Pedido[] | null>(null);
  readonly error = signal(false);

  ngOnInit(): void { this.load(); }

  load(): void {
    this.error.set(false);
    this.orders.list().subscribe({
      next: orders => this.data.set(orders),
      error: () => this.error.set(true)
    });
  }
}
