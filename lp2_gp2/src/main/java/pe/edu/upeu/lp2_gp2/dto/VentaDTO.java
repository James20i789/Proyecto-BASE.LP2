package pe.edu.upeu.lp2_gp2.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record VentaDTO(
        @NotNull // VALIDATION - NULA
        Long idPersona,
        @NotEmpty // VALIDATION - EMPTY
        // LISTAR Y LLAMAR A LA CLASE (ItemVentaDTO)
        List<ItemVentaDTO> items
){

}
