package pe.edu.upeu.lp2_gp2.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import pe.edu.upeu.lp2_gp2.entity.Usuario;

import java.util.Collection;
import java.util.List;

public record UsuarioPrincipal(Usuario usuario) implements UserDetails {
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(usuario.getRol().name()));
    }

    @Override
    public String getPassword() {
        return usuario.getPassword();
    }

    @Override
    public String getUsername() {
        return usuario.getUsuario();
    }
    // Metodo | IS ACCOUNT NO EXPIRABLE - VALOR VERDADERO
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    // Metodo Credencial | NO EXPIRADA
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
    // Metodo Enable | BOOLEAN - RETORNO DE VALOR VERDADERO
    @Override
    public boolean isEnabled() {
        return true;
    }
}
