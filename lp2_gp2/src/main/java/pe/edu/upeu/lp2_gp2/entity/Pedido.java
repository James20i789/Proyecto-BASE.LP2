package pe.edu.upeu.lp2_gp2.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "pedidos")
public class Pedido {

    @Id // CLASE PEDIDO
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idPedido;

    // Muchos pedidos ya tienen - IDPersona
    @ManyToOne
    @JoinColumn(name="id_persona")
    private Persona persona;

    // TOTAL | ESTADO
    private BigDecimal total;
    private String estado;

    // Registro FechaPEDIDOS
    @Column(name="fecha_pedido")
    private LocalDateTime fechaPedido;

    // MUchos detalles por ArrayList (ARREGLOS)
    @OneToMany(mappedBy = "pedido",cascade = CascadeType.ALL)
    private List<DetallePedido> detalle;

    public Pedido() {
    }

    public Pedido(Long idPedido, Persona persona, BigDecimal total, String estado, LocalDateTime fechaPedido, List<DetallePedido> detalle) {
        this.idPedido = idPedido;
        this.persona = persona;
        this.total = total;
        this.estado = estado;
        this.fechaPedido = fechaPedido;
        this.detalle = detalle;
    }

    public Long getIdPedido() {
        return idPedido;
    }

    public void setIdPedido(Long idPedido) {
        this.idPedido = idPedido;
    }

    public Persona getPersona() {
        return persona;
    }

    public void setPersona(Persona persona) {
        this.persona = persona;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public LocalDateTime getFechaPedido() {
        return fechaPedido;
    }

    public void setFechaPedido(LocalDateTime fechaPedido) {
        this.fechaPedido = fechaPedido;
    }

    public List<DetallePedido> getDetalle() {
        return detalle;
    }

    public void setDetalle(List<DetallePedido> detalle) {
        this.detalle = detalle;
    }
}

