package pe.edu.upeu.lp2_gp2.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ItemVentaDTO(
        @NotNull @Positive Long idProducto,
        @NotNull @Min(1) @Max(99) Integer cantidad
) {}
