package pe.edu.upeu.lp2_gp2.dto;

public record AuthResponse(
        String token,
        String usuario,
        String rol,
        Long idPersona
) {
}
