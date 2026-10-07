package pe.edu.upeu.lp2_gp2.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegistroDTO(

        // CAMPOS RECORD | ENVIAR DATOS
        @NotBlank(message = "EL NOMBRE ES OBLIGATORIO") // NO CAMPO VACÍO - BLANCO
        String nombre,
        @NotBlank(message = "El EMAIL ES OBLIGATORIO") // NO CAMPO VACÍO - BLANCO
        @Email(message = "Debe ser un email válido")
        String email,
        @NotBlank(message = "EL TELEFONO ES OBLIGATORIO") // NO CAMPO VACÍO - BLANCO
        @Pattern(regexp = "^9\\d{8}$", message = "EL TELEFONO DEBE TENER 9 DIGITOS Y EMPEZAR CON 9")
        String telefono,
        String direccion,
        @NotBlank(message = "EL USUARIO ES OBLIGATORIO") // NO CAMPO VACÍO - BLANCO
        @Size(min = 4, max = 20, message = "EL USUARIO DEBE TENER ENTRE 4 Y 20 CARACTERES")
        String usuario,
        @NotBlank(message = "LA CONTRASEÑA ES OBLIGATORIO") // NO CAMPO VACÍO - BLANCO
        @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
        String password

) {
}
