import { Producto } from "./producto.interface";

export interface CartItem {
    producto: Producto;
    cantidad: number;
}    