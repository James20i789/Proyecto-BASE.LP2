import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { VentaDTO } from '../Interfaces/pedido.interface';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class PedidoService {
  private http = inject(HttpClient);

  // API URL for pedido endpoints
  private apiUrl = '/api/v1/pedidos'; // RUTA DEL PROXY

  // CREAR PEDIDOS - PROCESO DE RUTA (VENTA)
  crearPedido(pedido: VentaDTO): Observable<any> {
    return this.http.post(this.apiUrl, pedido);
  }

  
}
