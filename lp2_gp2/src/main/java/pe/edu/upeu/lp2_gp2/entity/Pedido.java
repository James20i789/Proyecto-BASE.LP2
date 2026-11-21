package pe.edu.upeu.lp2_gp2.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pedidos", uniqueConstraints = @UniqueConstraint(
        name = "uq_pedido_intento", columnNames = {"id_persona", "clave_compra"}),
        indexes = @Index(name = "ix_pedido_vencimiento", columnList = "estado, vence_en"))
public class Pedido {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idPedido;

    @ManyToOne
    @JoinColumn(name = "id_persona")
    private Persona persona;

    private BigDecimal total;

    @Enumerated(EnumType.STRING)
    private EstadoPedido estado;

    @Column(name = "fecha_pedido")
    private LocalDateTime fechaPedido;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL)
    private List<DetallePedido> detalle = new ArrayList<>();

    @Column(name = "clave_compra", length = 36)
    private String claveCompra;

    @Column(name = "referencia_pago", length = 36, unique = true)
    private String referenciaPago;

    @Column(name = "vence_en")
    private Instant venceEn;

    @Column(name = "preferencia_pago")
    private String preferenciaPago;

    @Column(name = "checkout_url", length = 1800)
    private String checkoutUrl;

    @Column(name = "transaccion_pago", length = 32, unique = true)
    private String transaccionPago;

    @Column(name = "estado_pago", length = 32)
    private String estadoPago;

    public Long getIdPedido() { return idPedido; }
    public Persona getPersona() { return persona; }
    public void setPersona(Persona persona) { this.persona = persona; }
    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }
    public EstadoPedido getEstado() { return estado; }
    public void setEstado(EstadoPedido estado) { this.estado = estado; }
    public LocalDateTime getFechaPedido() { return fechaPedido; }
    public void setFechaPedido(LocalDateTime fechaPedido) { this.fechaPedido = fechaPedido; }
    public List<DetallePedido> getDetalle() { return detalle; }
    public void setClaveCompra(String claveCompra) { this.claveCompra = claveCompra; }
    public String getReferenciaPago() { return referenciaPago; }
    public void setReferenciaPago(String referenciaPago) { this.referenciaPago = referenciaPago; }
    public Instant getVenceEn() { return venceEn; }
    public void setVenceEn(Instant venceEn) { this.venceEn = venceEn; }
    public String getPreferenciaPago() { return preferenciaPago; }
    public void setPreferenciaPago(String preferenciaPago) { this.preferenciaPago = preferenciaPago; }
    public String getCheckoutUrl() { return checkoutUrl; }
    public void setCheckoutUrl(String checkoutUrl) { this.checkoutUrl = checkoutUrl; }
    public String getTransaccionPago() { return transaccionPago; }
    public void setTransaccionPago(String transaccionPago) { this.transaccionPago = transaccionPago; }
    public String getEstadoPago() { return estadoPago; }
    public void setEstadoPago(String estadoPago) { this.estadoPago = estadoPago; }
}
