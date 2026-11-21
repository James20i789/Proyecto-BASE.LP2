package pe.edu.upeu.lp2_gp2.Controller;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import pe.edu.upeu.lp2_gp2.dto.PedidoResponseDTO;
import pe.edu.upeu.lp2_gp2.dto.VentaDTO;
import pe.edu.upeu.lp2_gp2.security.UsuarioPrincipal;
import pe.edu.upeu.lp2_gp2.services.PedidoServiceImpl;

@RestController
@RequestMapping("/api/v1/pedidos")
public class PedidoController {
    private final PedidoServiceImpl orders;

    public PedidoController(PedidoServiceImpl orders) {
        this.orders = orders;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PedidoResponseDTO checkout(@AuthenticationPrincipal UsuarioPrincipal principal, @Valid @RequestBody VentaDTO dto) {
        return orders.checkout(principal.personaId(), dto);
    }

    @GetMapping("/{id}")
    public PedidoResponseDTO get(@AuthenticationPrincipal UsuarioPrincipal principal, @PathVariable Long id) {
        return orders.get(principal.personaId(), id);
    }

    @GetMapping
    public List<PedidoResponseDTO> list(@AuthenticationPrincipal UsuarioPrincipal principal) {
        return orders.list(principal.personaId());
    }
}
