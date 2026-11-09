package pe.edu.upeu.lp2_gp2.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetails userDetails;

    public JwtAuthenticationFilter(JwtService jwtService, UserDetails userDetails) {
        this.jwtService = jwtService;
        this.userDetails = userDetails;
    }

    @Override // FILTRO INTERNO
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    // CUMPLIR CON PERMISOS
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");
        // VARIABLE FINAL
        final String jwt;
        final String userEmail;

        // VALIDACIONES
        if(authHeader == null || !authHeader.startsWith("Bearer")){
            filterChain.doFilter(request, response);
            return;
        }
    }
}
