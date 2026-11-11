package pe.edu.upeu.lp2_gp2.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import pe.edu.upeu.lp2_gp2.exceptions.CustomAccessDeniedHandler;
import pe.edu.upeu.lp2_gp2.exceptions.CustomAuthenticationEntryPoint;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private final JwtAuthenticationFilter jwtFilter;
    private final AuthenticationProvider authenticationProvider;
    private final CustomAccessDeniedHandler accessDeniedHandler;
    private final CustomAuthenticationEntryPoint authenticationEntryPoint;

    public SecurityConfig(JwtAuthenticationFilter jwtFilter, AuthenticationProvider authenticationProvider, CustomAccessDeniedHandler accessDeniedHandler, CustomAuthenticationEntryPoint authenticationEntryPoint) {
        this.jwtFilter = jwtFilter;
        this.authenticationProvider = authenticationProvider;
        this.accessDeniedHandler = accessDeniedHandler;
        this.authenticationEntryPoint = authenticationEntryPoint;
    }
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{
        http
                .cors(org.springframework.security.config.Customizer.withDefaults()) // CONEXION DIRECTA A ANGULAR
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth->auth
                // RUTAS PUBLICAS
                                .requestMatchers("/api/v1/auth/**").permitAll()
                                .requestMatchers("/images/**").permitAll()
                                .requestMatchers("swagger-ui/**","v3/api-docs/**").permitAll()
                        // PRODUCTOS
                                .requestMatchers(HttpMethod.GET, "/api/v1/productos/**").permitAll() // CLIENTE VE EL CATÁLOGO
                                .requestMatchers("/api/v1/productos/**").hasAuthority("ADMIN") // SOLO EL ADMIN TIENE ACCESO
                        // PEDIDOS
                                .requestMatchers("/api/v1/pedidos/**").hasAuthority("CLIENTE")
                                .anyRequest().authenticated()
                )
                .sessionManagement(sess->
                        sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(exeption->exeption
                        . authenticationEntryPoint(authenticationEntryPoint) // MANEJA EL ERROR 401
                        .accessDeniedHandler(accessDeniedHandler) // MANEJA EL ERROR 403
                );
        // RETORNA LA CONTRUSCCIÓN DE LA WEB
        return http.build();
    }
}
