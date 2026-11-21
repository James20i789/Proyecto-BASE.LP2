import { computed, inject, Injectable, signal } from '@angular/core';
import { AlertService } from './alert.service';
import { CartItem } from '../Interfaces/cart-item.interface';
import { Producto } from '../Interfaces/producto.interface';
import { ItemVentaDTO } from '../Interfaces/pedido.interface';

interface Attempt {
  owner: number;
  items: string;
  key: string;
  orderId?: number;
}

@Injectable({ providedIn: 'root' })
export class CartService {
  private readonly alerts = inject(AlertService);
  private readonly cartItems = signal<CartItem[]>(this.loadCart());
  readonly items = this.cartItems.asReadonly();
  readonly totalItems = computed(() => this.items().reduce((total, item) => total + item.cantidad, 0));
  readonly totalPrice = computed(() =>
    this.items().reduce((total, item) => total + item.producto.precio * item.cantidad, 0));

  agregarProducto(producto: Producto): void {
    const existing = this.items().find(item => item.producto.id === producto.id);
    if (!producto.id || (existing?.cantidad ?? 0) >= Math.min(producto.stock, 99)) {
      this.alerts.toast('Alcanzaste la cantidad disponible.', 'warning');
      return;
    }
    const updated = { producto, cantidad: (existing?.cantidad ?? 0) + 1 };
    this.save(existing ? this.items().map(item => item === existing ? updated : item) : [...this.items(), updated]);
    this.alerts.toast(`${producto.nombre} añadido al carrito`);
  }

  eliminarItem(id: number): void {
    this.save(this.items().filter(item => item.producto.id !== id));
  }

  actualizarCantidad(id: number, delta: number): void {
    this.save(this.items().map(item => {
      if (item.producto.id !== id) return item;
      const quantity = item.cantidad + delta;
      return quantity >= 1 && quantity <= Math.min(item.producto.stock, 99) ? { ...item, cantidad: quantity } : item;
    }));
  }

  checkout(owner: number): { key: string; items: ItemVentaDTO[] } {
    const items = this.payload();
    const snapshot = JSON.stringify(items);
    const current = this.getAttempt();
    if (current?.owner === owner && current.items === snapshot) {
      return { key: current.key, items };
    }
    const attempt = { owner, items: snapshot, key: crypto.randomUUID() };
    localStorage.setItem('cart_checkout', JSON.stringify(attempt));
    return { key: attempt.key, items };
  }

  rememberOrder(key: string, orderId: number): void {
    const attempt = this.getAttempt();
    if (attempt?.key === key) {
      localStorage.setItem('cart_checkout', JSON.stringify({ ...attempt, orderId }));
    }
  }

  finishOrder(owner: number, orderId: number, paid: boolean): void {
    const attempt = this.getAttempt();
    if (attempt?.owner !== owner || attempt.orderId !== orderId) return;
    if (paid && attempt.items === JSON.stringify(this.payload())) {
      this.save([]);
    }
    localStorage.removeItem('cart_checkout');
  }

  private payload(): ItemVentaDTO[] {
    return this.items().map(item => ({ idProducto: item.producto.id!, cantidad: item.cantidad }))
      .sort((a, b) => a.idProducto - b.idProducto);
  }

  private loadCart(): CartItem[] {
    try {
      const stored = JSON.parse(localStorage.getItem('cart_ecommerce') ?? '[]');
      return Array.isArray(stored) ? stored.filter((item: CartItem) =>
        Number.isSafeInteger(item?.producto?.id) && Number.isFinite(item.producto.precio)
        && Number.isInteger(item.cantidad) && item.cantidad > 0 && item.cantidad <= 99) : [];
    } catch {
      return [];
    }
  }

  private getAttempt(): Attempt | null {
    try {
      return JSON.parse(localStorage.getItem('cart_checkout') ?? 'null');
    } catch {
      return null;
    }
  }

  private save(items: CartItem[]): void {
    this.cartItems.set(items);
    localStorage.setItem('cart_ecommerce', JSON.stringify(items));
  }
}
