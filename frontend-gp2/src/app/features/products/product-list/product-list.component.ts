import { CurrencyPipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { ProductoService } from '../../../core/services/producto.service';
import { Producto } from '../../../core/Interfaces/producto.interface';
import { AlertService } from '../../../core/services/alert.service';

@Component({
  selector: 'app-product-list',
  standalone: true,
  imports: [CurrencyPipe, RouterLink],
  templateUrl: './product-list.component.html',
  styleUrl: './product-list.component.css'
})
export class ProductListComponent implements OnInit {
  private readonly catalog = inject(ProductoService);
  private readonly alerts = inject(AlertService);
  readonly products = signal<Producto[] | null>(null);
  readonly error = signal(false);

  ngOnInit(): void { this.listarProductos(); }

  listarProductos(): void {
    this.error.set(false);
    this.catalog.getProductos().subscribe({
      next: data => this.products.set(data),
      error: () => this.error.set(true)
    });
  }

  async eliminar(product: Producto): Promise<void> {
    if (!product.id || !await this.alerts.confirm(`¿Eliminar «${product.nombre}» de la carta?`)) return;
    this.catalog.deleteProducto(product.id).subscribe({
      next: () => {
        this.alerts.toast('Producto eliminado');
        this.listarProductos();
      },
      error: (error: HttpErrorResponse) => this.alerts.error(error.error?.message || 'No se pudo eliminar el producto.')
    });
  }
}
