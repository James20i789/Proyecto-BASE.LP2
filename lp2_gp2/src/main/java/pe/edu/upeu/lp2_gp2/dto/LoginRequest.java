package pe.edu.upeu.lp2_gp2.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "EL USER ES OBLIGATORIO")
        String usuario,
        @NotBlank(message = "LA CONTRASEÑA ES OBLIGATORIA")
        String password
) {
}
