package pe.edu.upeu.lp2_gp2.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import io.swagger.v3.oas.annotations.Parameter;

import java.math.BigDecimal;

public record ProductoDTO(

        @Parameter (hidden = true)
        Long id,
        @NotBlank(message = "El nombre es obligatorio")
        String nombre,
        String descripcion,
        @NotNull(message = "El precio es obligatorio")
        @DecimalMin(value = "0.01")
        BigDecimal precio,
        @NotNull(message = "El stock es obligatorio")
        Integer stock,
        @Parameter (hidden = true)
        @Schema(accessMode = Schema.AccessMode.READ_ONLY)
        String imagen

) {
}
