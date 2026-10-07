import { Component, inject, OnInit } from '@angular/core';
import { ProductoService } from '../../core/services/producto.service';
import { Producto } from '../../core/Interfaces/producto.interface';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [],
  templateUrl: './home.component.html',
  styleUrl: './home.component.css'
})
export class HomeComponent implements OnInit {
  // NOS CONECTAMOS CON SERVICIO DE PRODUCTOS PARA PODER USARLO EN EL COMPONENTE HOME
  private productoService = inject(ProductoService);
  
  productos: Producto[] = [];

  ngOnInit(): void {
    this.loadingProducts();
  }
  // CARGAMOS LOS PRODUCTOS DESDE EL SERVICIO DE PRODUCTOS
  loadingProducts(){
    this.productoService.getProductos().subscribe({
      next:(data) => this.productos = data,
      error:(err) => console.error('Error al cargar los productos', err)
    });
  } 
}

