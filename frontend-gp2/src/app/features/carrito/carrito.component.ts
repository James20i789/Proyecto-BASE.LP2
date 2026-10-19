import { Component, inject } from '@angular/core';
import { CartService } from '../../core/services/cart.service';
import { AlertService } from '../../core/services/alert.service';
import { Router, RouterLink } from '@angular/router';
import { ItemVentaDTO, VentaDTO } from '../../core/Interfaces/pedido.interface';

@Component({
  selector: 'app-carrito',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './carrito.component.html',
  styleUrl: './carrito.component.css'
})
export class CarritoComponent {
public cartService = inject(CartService);
  
pagar() {

}
}
