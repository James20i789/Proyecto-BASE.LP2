import { Component, inject, OnInit } from '@angular/core';
import { ProductoService } from '../../core/services/producto.service';
import { Producto } from '../../core/Interfaces/producto.interface';
import { CartService } from '../../core/services/cart.service';

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
  private cartService = inject(CartService);
  
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
  // MÉTODO PARA AGREGAR AL CARRITO DESDE EL COMPONENTE HOME
  agregarAlCarrito(producto: Producto){
    this.cartService.agregarProducto(producto);
  }
}

