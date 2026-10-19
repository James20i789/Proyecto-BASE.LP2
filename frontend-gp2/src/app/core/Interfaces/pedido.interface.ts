export interface ItemVentaDTO {
    idProducto: number;
    cantidad: number;
}
export interface VentaDTO {
    idPersona : number;
    items: ItemVentaDTO[];
}
