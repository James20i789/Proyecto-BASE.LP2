package pe.edu.upeu.lp2_gp2.dto;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record ProductoDTO(
        @Parameter(hidden = true) Long id,
        @NotBlank(message = "El nombre es obligatorio") @Size(max = 240) String nombre,
        @Size(max = 255) String descripcion,
        @NotNull @DecimalMin("0.01") @Digits(integer = 10, fraction = 2) BigDecimal precio,
        @NotNull @PositiveOrZero Integer stock,
        @Parameter(hidden = true) @Schema(accessMode = Schema.AccessMode.READ_ONLY) String imagen
) {}
