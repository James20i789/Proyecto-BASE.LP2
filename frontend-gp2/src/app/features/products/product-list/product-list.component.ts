import { CommonModule } from '@angular/common';
import { Component, inject, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { ProductoService } from '../../../core/services/producto.service';
import { Producto } from '../../../core/Interfaces/producto.interface';

@Component({
  selector: 'app-product-list',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './product-list.component.html',
  styleUrl: './product-list.component.css'
})
export class ProductListComponent implements OnInit {
  private productoService = inject(ProductoService);
  products: Producto[] = [];
  ngOnInit(): void {
    this.listarProductos();
  }
  listarProductos(){
    this.productoService.getProductos().subscribe
    (data => this.products=data)
  }
}
