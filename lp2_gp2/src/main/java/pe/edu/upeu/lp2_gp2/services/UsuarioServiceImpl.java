package pe.edu.upeu.lp2_gp2.services;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.lp2_gp2.dto.RegistroDTO;
import pe.edu.upeu.lp2_gp2.dto.UsuarioDTO;
import pe.edu.upeu.lp2_gp2.entity.Persona;
import pe.edu.upeu.lp2_gp2.entity.Rol;
import pe.edu.upeu.lp2_gp2.entity.Usuario;
import pe.edu.upeu.lp2_gp2.exceptions.DuplicateResourceException;
import pe.edu.upeu.lp2_gp2.repository.PersonaRepository;
import pe.edu.upeu.lp2_gp2.repository.UsuarioRepository;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UsuarioServiceImpl implements IUsuarioService{

    // CLASE PRIVADAS FINALES PARA UNA VERIDICA CONEXIÓN.
    private final PersonaRepository personaRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    // USUARIO | IMPLEMENTACIÓN - SERVICIOS
    public UsuarioServiceImpl(PersonaRepository personaRepository, UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.personaRepository = personaRepository;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }


    // DATOS TRANSACTIONALS
    @Override
    @Transactional // UsuarioDTO | REGISTRAR CLIENTE
    public Optional<UsuarioDTO> registrarCliente(RegistroDTO dto) {

        String email = dto.email().strip().toLowerCase(Locale.ROOT);
        if (dto.password().getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La contraseña supera los 72 bytes.");
        }
        if (personaRepository.existsByEmailIgnoreCase(email) || usuarioRepository.existsByUsuario(dto.usuario())) {
            throw new DuplicateResourceException("El usuario o correo ya está registrado.");
        }

        // Envío de datos | PROCESO CORRECTO
        Persona p = new Persona();
        p.setNombre(dto.nombre().strip());
        p.setEmail(email);
        p.setTelefono(dto.telefono());
        p.setDireccion(dto.direccion());
        Persona personaGuardada = personaRepository.save(p);

        Usuario u = new Usuario();
        u.setUsuario(dto.usuario());
        u.setNombre(dto.usuario());
        // Encriptamos la clave con spring security passwordEncoder
        String passwordEncriptado = passwordEncoder.encode(dto.password());
        u.setPassword(passwordEncriptado);
        u.setRol(Rol.CLIENTE);
        u.setPersona(personaGuardada);

        // USUARIO GUARDADO - REPOSITORY
        Usuario usuarioGuardado = usuarioRepository.save(u);
        return Optional.of(new UsuarioDTO(
                usuarioGuardado.getIdUsuario(),
                usuarioGuardado.getUsuario(),
                usuarioGuardado.getRol().name(),
                usuarioGuardado.getPersona().getNombre(),
                usuarioGuardado.getPersona().getEmail()
        ));
    }
}
