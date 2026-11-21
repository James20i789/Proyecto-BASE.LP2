package pe.edu.upeu.lp2_gp2.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegistroDTO(
        @NotBlank @Size(max = 100) String nombre,
        @NotBlank @Email @Size(max = 254) String email,
        @NotBlank @Pattern(regexp = "9[0-9]{8}", message = "Ingresa un celular peruano de 9 dígitos") String telefono,
        @Size(max = 255) String direccion,
        @NotBlank @Size(min = 4, max = 20)
        @Pattern(regexp = "[a-zA-Z0-9_.-]+", message = "Usa letras, números, puntos, guiones o guiones bajos") String usuario,
        @NotBlank @Size(min = 12, max = 72, message = "Usa entre 12 y 72 caracteres") String password
) {
}
