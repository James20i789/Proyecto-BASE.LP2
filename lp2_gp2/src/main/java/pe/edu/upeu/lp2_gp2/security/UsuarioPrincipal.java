package pe.edu.upeu.lp2_gp2.security;

import java.io.Serializable;
import java.util.List;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import pe.edu.upeu.lp2_gp2.dto.AuthResponse;
import pe.edu.upeu.lp2_gp2.entity.Usuario;

public record UsuarioPrincipal(String username, String role, Long personaId, String password)
        implements UserDetails, Serializable {
    public UsuarioPrincipal(Usuario user) {
        this(user.getUsuario(), user.getRol().name(), user.getPersona().getIdPersona(), user.getPassword());
    }

    @Override
    public List<SimpleGrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role));
    }

    @Override
    public String getUsername() { return username; }

    @Override
    public String getPassword() { return password; }

    public UsuarioPrincipal withoutPassword() {
        return new UsuarioPrincipal(username, role, personaId, null);
    }

    public AuthResponse profile() {
        return new AuthResponse(username, role, personaId);
    }
}
