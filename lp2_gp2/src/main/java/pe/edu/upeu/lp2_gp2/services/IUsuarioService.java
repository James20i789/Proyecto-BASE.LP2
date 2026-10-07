package pe.edu.upeu.lp2_gp2.services;

import pe.edu.upeu.lp2_gp2.dto.RegistroDTO;
import pe.edu.upeu.lp2_gp2.dto.UsuarioDTO;
import pe.edu.upeu.lp2_gp2.entity.Persona;
import pe.edu.upeu.lp2_gp2.entity.Usuario;

import java.util.Optional;

public interface IUsuarioService {

    // VARIABLES - INTERFACE (ARTEFACTOS - METODOS A IMPLEMENTAR) | USUARIO
    Optional<UsuarioDTO>registrarCliente(RegistroDTO dto);

}
