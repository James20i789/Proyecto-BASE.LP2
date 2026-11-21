import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { API } from '../api';
import { Pedido, VentaDTO } from '../Interfaces/pedido.interface';

@Injectable({ providedIn: 'root' })
export class PedidoService {
  private readonly http = inject(HttpClient);
  private readonly url = `${API}/pedidos`;

  crearPedido(pedido: VentaDTO) {
    return this.http.post<Pedido>(this.url, pedido);
  }

  get(id: number) {
    return this.http.get<Pedido>(`${this.url}/${id}`);
  }

  list() {
    return this.http.get<Pedido[]>(this.url);
  }
}
