import { CurrencyPipe } from '@angular/common';
import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Producto } from '../../core/Interfaces/producto.interface';
import { CartService } from '../../core/services/cart.service';
import { ProductoService } from '../../core/services/producto.service';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CurrencyPipe, RouterLink],
  templateUrl: './home.component.html',
  styleUrl: './home.component.css'
})
export class HomeComponent implements OnInit {
  readonly cart = inject(CartService);
  private readonly catalog = inject(ProductoService);
  readonly products = signal<Producto[]>([]);
  readonly loading = signal(true);
  readonly failed = signal(false);
  readonly query = signal('');
  readonly filtered = computed(() => {
    const term = this.fold(this.query().trim());
    return this.products().filter(product => this.fold(`${product.nombre} ${product.descripcion}`).includes(term));
  });

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.failed.set(false);
    this.catalog.getProductos().subscribe({
      next: products => {
        this.products.set(products);
        this.loading.set(false);
      },
      error: () => {
        this.failed.set(true);
        this.loading.set(false);
      }
    });
  }

  private fold(text: string): string {
    return text.normalize('NFD').replace(/\p{Diacritic}/gu, '').toLowerCase();
  }
}
