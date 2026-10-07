package pe.edu.upeu.lp2_gp2.entity;


import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name="detalle_pedido")
public class DetallePedido {

    @Id // CLASE DETALLE PEDIDO
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idDetalle;

    // PEDIDO
    @ManyToOne
    @JoinColumn(name="id_pedido")
    private Pedido pedido;

    // PRODUCTO
    @ManyToOne
    @JoinColumn(name="id_producto")
    private Producto producto;

    private Integer cantidad;

    private BigDecimal precioUnitario;

    private BigDecimal subTotal;

    public DetallePedido() {
    }

    // CONSTRUCTOR COMPLETO DETALLE-PEDIDO
    public DetallePedido(Long idDetalle, Pedido pedido, Producto producto, Integer cantidad, BigDecimal precioUnitario, BigDecimal subTotal) {
        this.idDetalle = idDetalle;
        this.pedido = pedido;
        this.producto = producto;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.subTotal = subTotal;
    }

    // GETTER AND SETTER - CLASE DETALLE-PEDIDO
    public Long getIdDetalle() {
        return idDetalle;
    }

    // GETTERS AND SETTERS - CLASE DETALLE PEDIDO
    public void setIdDetalle(Long idDetalle) {
        this.idDetalle = idDetalle;
    }

    public Pedido getPedido() {
        return pedido;
    }

    public void setPedido(Pedido pedido) {
        this.pedido = pedido;
    }

    public Producto getProducto() {
        return producto;
    }

    public void setProducto(Producto producto) {
        this.producto = producto;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(BigDecimal precioUnitario) {
        this.precioUnitario = precioUnitario;
    }

    public BigDecimal getSubTotal() {
        return subTotal;
    }

    public void setSubTotal(BigDecimal subTotal) {
        this.subTotal = subTotal;
    }
}
