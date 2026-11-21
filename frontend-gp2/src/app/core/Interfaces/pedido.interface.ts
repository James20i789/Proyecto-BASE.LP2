export interface ItemVentaDTO {
  idProducto: number;
  cantidad: number;
}

export interface VentaDTO {
  intento: string;
  items: ItemVentaDTO[];
}

export type EstadoPedido = 'PENDIENTE' | 'PAGADO' | 'VENCIDO' | 'REEMBOLSO_PENDIENTE'
  | 'REEMBOLSADO' | 'REEMBOLSO_PARCIAL' | 'CONTRACARGO';

export interface Pedido {
  idPedido: number;
  cliente: string;
  total: number;
  estado: EstadoPedido;
  fecha: string;
  productos: { producto: string; cantidad: number; precioUnitario: number; subtotal: number }[];
  venceEn: string | null;
  checkoutUrl: string | null;
  estadoPago: string | null;
}

export const ESTADO_PEDIDO: Record<EstadoPedido, string> = {
  PENDIENTE: 'Pago pendiente',
  PAGADO: 'Pago confirmado',
  VENCIDO: 'Pedido vencido',
  REEMBOLSO_PENDIENTE: 'Reembolso en proceso',
  REEMBOLSADO: 'Pago reembolsado',
  REEMBOLSO_PARCIAL: 'Reembolso parcial',
  CONTRACARGO: 'Pago en disputa'
};
