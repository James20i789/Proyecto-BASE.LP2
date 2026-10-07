package pe.edu.upeu.lp2_gp2.dto;

public record UsuarioDTO(

        // CAMPOS RECORD | ENVIAR DATOS
        Long idUsuario,
        String Usuario,
        String rol,
        String nombrePersona,
        String email
) {
}
