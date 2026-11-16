import { Component, inject } from '@angular/core';
import { CartService } from '../../core/services/cart.service';
import { AlertService } from '../../core/services/alert.service';
import { Router, RouterLink } from '@angular/router';
import { ItemVentaDTO, VentaDTO } from '../../core/Interfaces/pedido.interface';
import { PedidoService } from '../../core/services/pedido.service';

@Component({
  selector: 'app-carrito',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './carrito.component.html',
  styleUrl: './carrito.component.css'
}) // CLASE EXPORTADA | CARRITO COMPONENT
export class CarritoComponent {
  // Inyectar servicios necesarios
  public cartService = inject(CartService);
  private pedidoService = inject(PedidoService);
  private alertService = inject(AlertService);
  private router = inject(Router);

  // Método para realizar el pago y crear un pedido
async pagar() {
  // (1) PRIMERO VERIFICAR QUE EL USUARIO SE ENCUENTRE LOGEADO
  const token = localStorage.getItem('token_ecommerce'); // RECUPERAR EL TOKEN DEL LOCALSTORAGE
  if (!token) {
    this.alertService.error('error', 'Debes iniciar sesión para realizar el pago.'); // MOSTRAR ALERTA DE ERROR
    return; // SALIR DEL MÉTODO SI NO HAY TOKEN
  }
  const confirmado = await this.alertService.confirm('¿Estás seguro de realizar el pago?'); // MOSTRAR ALERTA DE CONFIRMACIÓN
  if (!confirmado) return; // SALIR DEL MÉTODO SI EL USUARIO NO CONFIRMA

  // (2) OBTENER EL ID DE LA PERSONA YA LOGEADA
    const idPersona = Number(localStorage.getItem('user_id_persona')); // RECUPERAR EL ID DE LA PERSONA DEL LOCALSTORAGE
  
  // (3) MAPEAR LOS ITEMS DEL CARRITO A ItemVentaDTO - FORMATO DTO DEL BACKEND
    const itemsEnvio : ItemVentaDTO[] = this.cartService.getItems().map(item => ({
      idProducto: item.producto.id!,
      cantidad: item.cantidad
    }));
    
  // (4) CONSTRUIR EL OBJETO VentaDTO PARA ENVIAR AL BACKEND - FASE FINAL
  const pedidoFinal: VentaDTO = {
    idPersona: idPersona,
    items: itemsEnvio
  };

  // (5) ENVIAR EL PEDIDO AL BACKEND PARA SU PROCESAMIENTO
  this.pedidoService.crearPedido(pedidoFinal).subscribe({
    next: (res) => {
      this.alertService.success('Pago realizado con éxito.');
      this.cartService.vaciarCarrito(); // LIMPIAR EL CARRITO DESPUÉS DEL PAGO
      this.router.navigate(['/home']); // REDIRECCIONAR AL USUARIO A LA PÁGINA DE INICIO
    },error: (err) => {
      // Manejar errores de la solicitud
      const msg = err.error.message || 'Error al procesar el pago.';
      this.alertService.error(msg, 'Error en el pedido.'); // MOSTRAR ALERTA DE ERROR SI HAY PROBLEMAS CON EL PAGO
    }
});

}
}
