package pe.edu.upeu.lp2_gp2.repository;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pe.edu.upeu.lp2_gp2.entity.Persona;

@Repository
public interface PersonaRepository  extends JpaRepository<Persona, Long> {
    boolean existsByEmailIgnoreCase(String email);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Persona p where p.idPersona = :id")
    Optional<Persona> lockById(Long id);
}
