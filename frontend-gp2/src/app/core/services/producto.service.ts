import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable, tap } from 'rxjs';
import { Producto } from '../Interfaces/producto.interface';

// TRAE EL SERVCIO DE PRODUCTOS PARA PODER USARLO EN EL COMPONENTE HOME
@Injectable({
  providedIn: 'root'
})
export class ProductoService {

  private http = inject(HttpClient);
  // USAMOS LA RUTA DEL BACKEND PARA OBTENER LOS PRODUCTOS
  private apiUrl = '/api/v1/productos';

  // LISTAR | RUTA PUBLICA SIN NECESIDAD DE TOKENS
  getProductos(): Observable<Producto[]> {
  return this.http.get<Producto[]>(this.apiUrl).pipe(
    tap((data) => {
      console.log('Productos recibidos:', data);
    })
  );
}

  // OBTENER | MEDIANTE UN ID ESPECIFICO
  getProductoById(id: number): Observable<Producto> {
    return this.http.get<Producto>(`${this.apiUrl}/${id}`); //CONCATENAMOS LA URL CON EL ID DEL PRODUCTO
    }
    // METODOS PARA EL ADMINISTRADOR (POST | CREAR-PRODUCTO)
    createProducto(formData: FormData): Observable<Producto> {
      // DATOS A INSERTAR POR CONSOLA
      console.log("Form Data a recibir en el servicio: CREATE PRODUCTO");
      formData.forEach((value, key) => {
        console.log(`${key}: ${value}`);
      });
      return this.http.post<Producto>(this.apiUrl, formData);
    }
    // METODOS PARA EL ADMINISTRADOR (PUT | ACTUALIZAR-PRODUCTO)
    updateProducto(id:number, formData: FormData): Observable<Producto> {
      // DATOS A INSERTAR POR CONSOLA - VIA PUT
      console.log("Form Data a recibir en el servicio: UPDATE PRODUCTO");
      formData.forEach((value, key) => {
        console.log(`${key}: ${value}`);
      });
      return this.http.put<Producto>(`${this.apiUrl}/${id}`, formData);
    }
    // METODOS PARA EL ADMINISTRADOR (DELETE-PRODUCTO)
    deleteProducto(id: number): Observable<void> {
      console.log("ID DEL PRODUCTO A ELIMINAR: ", id);
      return this.http.delete<void>(`${this.apiUrl}/${id}`);
    }

  }
