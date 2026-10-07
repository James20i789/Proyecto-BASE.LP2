package pe.edu.upeu.lp2_gp2.dto;


import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.upeu.lp2_gp2.services.IPedidoService;

@RestController
@RequestMapping("/api/v1/pedidos")
public class PedidoController {
    private final IPedidoService pedidoService;

    public PedidoController(IPedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    @PostMapping
    public ResponseEntity<PedidoResponseDTO> crearPedido(@Valid @RequestBody VentaDTO dto) {
        PedidoResponseDTO response = pedidoService.realizarPedido(dto);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}

