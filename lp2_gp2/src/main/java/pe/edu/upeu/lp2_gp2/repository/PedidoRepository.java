package pe.edu.upeu.lp2_gp2.repository;

import jakarta.persistence.LockModeType;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import pe.edu.upeu.lp2_gp2.entity.EstadoPedido;
import pe.edu.upeu.lp2_gp2.entity.Pedido;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    List<Pedido> findTop30ByPersonaIdPersonaOrderByIdPedidoDesc(Long idPersona);
    Optional<Pedido> findByIdPedidoAndPersonaIdPersona(Long id, Long idPersona);
    Optional<Pedido> findByPersonaIdPersonaAndClaveCompra(Long idPersona, String clave);
    long countByPersonaIdPersonaAndEstadoAndVenceEnAfter(Long idPersona, EstadoPedido estado, Instant ahora);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Pedido p where p.idPedido = :id")
    Optional<Pedido> lockById(Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Pedido> findByReferenciaPago(String referenciaPago);

    @Query("select p.idPedido from Pedido p where p.estado = :estado and p.venceEn <= :hasta order by p.venceEn")
    List<Long> due(EstadoPedido estado, Instant hasta, Pageable pageable);
}
