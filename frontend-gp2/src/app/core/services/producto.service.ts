import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { API } from '../api';
import { Producto } from '../Interfaces/producto.interface';

// TRAE EL SERVCIO DE PRODUCTOS PARA PODER USARLO EN EL COMPONENTE HOME
@Injectable({
  providedIn: 'root'
})
export class ProductoService {

  private http = inject(HttpClient);
  // USAMOS LA RUTA DEL BACKEND PARA OBTENER LOS PRODUCTOS
  private apiUrl = `${API}/productos`;

  // LISTAR | RUTA PUBLICA SIN NECESIDAD DE TOKENS
  getProductos(): Observable<Producto[]> {
    return this.http.get<Producto[]>(this.apiUrl);
  }

  getProductoById(id: number): Observable<Producto> {
    return this.http.get<Producto>(`${this.apiUrl}/${id}`);
  }

  createProducto(formData: FormData): Observable<Producto> {
    return this.http.post<Producto>(this.apiUrl, formData);
  }

  updateProducto(id: number, formData: FormData): Observable<Producto> {
    return this.http.put<Producto>(`${this.apiUrl}/${id}`, formData);
  }

  deleteProducto(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
