package pe.edu.upeu.lp2_gp2.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;

public record VentaDTO(
        Long idPersona,
        @NotNull UUID intento,
        @NotEmpty @Size(max = 40) List<@NotNull @Valid ItemVentaDTO> items
) {}
