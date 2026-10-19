import { CommonModule } from '@angular/common';
import { Component, inject, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { ProductoService } from '../../../core/services/producto.service';
import { Producto } from '../../../core/Interfaces/producto.interface';
import { AlertService } from '../../../core/services/alert.service';

@Component({
  selector: 'app-product-list',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './product-list.component.html',
  styleUrl: './product-list.component.css'
})
export class ProductListComponent implements OnInit {
  private productoService = inject(ProductoService);
  private alertService = inject(AlertService);

  products: Producto[] = [];
  ngOnInit(): void {
    this.listarProductos();
  }
  listarProductos(){
    this.productoService.getProductos().subscribe
    (data => this.products=data)
  }
  async eliminar(id:number){
    const confirmado = await this.alertService.confirm('Esta accion no se puede deshacer. ¿Desea continuar?');
    if (confirmado) return;
    this.productoService.deleteProducto(id).subscribe({
      next: () => {
        this.alertService.toast('Producto eliminado con éxito');
        this.listarProductos();
      }, error: () => this.alertService.error('Ocurrió un error al eliminar el producto')
    });
  }
}
