package pe.edu.upeu.lp2_gp2.dto;

import java.math.BigDecimal;

public record DetalleResponseDTO(
        String producto,
        Integer cantidad,
        BigDecimal precioUnitario,
        BigDecimal subtotal
) {
}
