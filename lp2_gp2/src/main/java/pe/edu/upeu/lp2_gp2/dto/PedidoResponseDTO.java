package pe.edu.upeu.lp2_gp2.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record PedidoResponseDTO(
        Long idPedido,
        String cliente,
        BigDecimal total,
        String estado,
        LocalDateTime fecha,
        // LISTAR Y LLAMAR A LA CLASE (DetalleRespondeDTO)
        List<DetalleResponseDTO>productos
) {
}
