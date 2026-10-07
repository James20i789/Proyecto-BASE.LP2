package pe.edu.upeu.lp2_gp2.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record ItemVentaDTO(
        @NotNull // VALIDATION - NULA
        Long idProducto,
        @Min(1) // VALIDATION - MIN
        Integer cantidad


) {
}
