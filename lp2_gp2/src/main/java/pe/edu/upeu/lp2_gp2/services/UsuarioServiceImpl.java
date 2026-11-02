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

import java.util.Optional;

@Service
public class UsuarioServiceImpl implements IUsuarioService{

    private final PersonaRepository personaRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioServiceImpl(PersonaRepository personaRepository, UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.personaRepository = personaRepository;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }


    // DATOS TRANSACTIONALS
    @Override
    @Transactional // UsuarioDTO | REGISTRAR CLIENTE
    public Optional<UsuarioDTO> registrarCliente(RegistroDTO dto) {

        // VALIDAR QUE EL EMAIL SEA ÚNICO
        if(personaRepository.existsByEmail(dto.email())){
            throw new DuplicateResourceException("EL EMAIL´" + dto.email() + "´YA ESTA REGISTRADO");
        }
        // VALIDAR QUE EL USUARIO SEA ÚNICO
        if(usuarioRepository.existsByUsuario(dto.usuario())){
            throw new DuplicateResourceException("EL NOMBRE DEL USUARIO´" + dto.usuario() + "´YA EXISTE");
        }
        // SI TODO VA BIEN, PROCEDEMOS A ENVIAR LOS DATOS
        Persona p = new Persona();
        p.setNombre(dto.nombre());
        p.setEmail(dto.email());
        p.setTelefono(dto.telefono());
        p.setDireccion(dto.direccion());
        Persona personaGuardada = personaRepository.save(p);

        Usuario u = new Usuario();
        u.setUsuario(dto.usuario());
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
