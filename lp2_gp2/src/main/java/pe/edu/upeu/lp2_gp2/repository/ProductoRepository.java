package pe.edu.upeu.lp2_gp2.repository;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import pe.edu.upeu.lp2_gp2.entity.Producto;

public interface ProductoRepository extends JpaRepository<Producto, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Producto p where p.idProducto = :id")
    Optional<Producto> lockById(Long id);
}
