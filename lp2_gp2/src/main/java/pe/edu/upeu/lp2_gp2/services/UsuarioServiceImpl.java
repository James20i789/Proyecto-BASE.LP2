package pe.edu.upeu.lp2_gp2.services;

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

    public UsuarioServiceImpl(PersonaRepository personaRepository, UsuarioRepository usuarioRepository) {
        this.personaRepository = personaRepository;
        this.usuarioRepository = usuarioRepository;
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
        Persona p = new Persona(); // INSTANCIA - Clase Persona
        p.setNombre(dto.nombre());
        p.setEmail(dto.email());
        p.setTelefono(dto.telefono());
        p.setDireccion(dto.direccion());
        Persona personaGuarda = personaRepository.save(p);

        Usuario u = new Usuario(); // INSTANCIA - Clase Usuario
        u.setUsuario(dto.usuario());

        u.setPassword(dto.password());
        u.setRol(Rol.CLIENTE);
        u.setPersona(personaGuarda);

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
