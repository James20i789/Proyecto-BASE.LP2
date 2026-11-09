package pe.edu.upeu.lp2_gp2.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import pe.edu.upeu.lp2_gp2.repository.UsuarioRepository;
import pe.edu.upeu.lp2_gp2.security.UsuarioPrincipal;

@Configuration
public class ApplicationConfig {
    private final UsuarioRepository usuarioRepository;

    public ApplicationConfig(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }
    @Bean // USER DETAILS SERVICE
    public UserDetailsService userDetailsService(){
        return username -> usuarioRepository.findByUsuario(username)
                .map(UsuarioPrincipal::new)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
    }
    @Bean // AUTHENTICATION PROVIDER
    public AuthenticationProvider authenticationProvider(){
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService());
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }
    @Bean // AUTHENTICATION MANAGER
    public AuthenticationManager authenticationManager (AuthenticationConfiguration config) throws Exception{
        return config.getAuthenticationManager();
    }
    @Bean // PASSWORD ENCODER
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }
}