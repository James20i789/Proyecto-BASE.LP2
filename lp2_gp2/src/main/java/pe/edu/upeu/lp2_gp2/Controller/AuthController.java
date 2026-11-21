package pe.edu.upeu.lp2_gp2.Controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.nio.charset.StandardCharsets;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;
import pe.edu.upeu.lp2_gp2.dto.AuthResponse;
import pe.edu.upeu.lp2_gp2.dto.LoginRequest;
import pe.edu.upeu.lp2_gp2.dto.RegistroDTO;
import pe.edu.upeu.lp2_gp2.dto.UsuarioDTO;
import pe.edu.upeu.lp2_gp2.security.LoginAttempts;
import pe.edu.upeu.lp2_gp2.security.UsuarioPrincipal;
import pe.edu.upeu.lp2_gp2.services.IUsuarioService;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final IUsuarioService users;
    private final AuthenticationManager authentication;
    private final HttpSessionSecurityContextRepository sessions;
    private final CookieCsrfTokenRepository csrfCookies;
    private final LoginAttempts attempts;

    public AuthController(IUsuarioService users, AuthenticationManager authentication,
                          HttpSessionSecurityContextRepository sessions, CookieCsrfTokenRepository csrfCookies,
                          LoginAttempts attempts) {
        this.users = users;
        this.authentication = authentication;
        this.sessions = sessions;
        this.csrfCookies = csrfCookies;
        this.attempts = attempts;
    }

    @GetMapping("/csrf")
    public ResponseEntity<Void> csrf(CsrfToken token) {
        token.getToken();
        return ResponseEntity.noContent().cacheControl(CacheControl.noStore()).build();
    }

    @GetMapping("/session")
    public ResponseEntity<AuthResponse> session(@AuthenticationPrincipal UsuarioPrincipal principal) {
        if (principal == null) {
            return ResponseEntity.noContent().cacheControl(CacheControl.noStore()).build();
        }
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(principal.profile());
    }

    @PostMapping("/registro")
    public ResponseEntity<UsuarioDTO> register(@Valid @RequestBody RegistroDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(users.registrarCliente(dto).orElseThrow());
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest login, HttpServletRequest request,
                                              HttpServletResponse response) {
        String username = login.usuario().strip();
        attempts.check(request.getRemoteAddr(), username);
        if (login.password().getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new BadCredentialsException("Credenciales inválidas");
        }
        var authenticated = authentication.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(username, login.password()));
        attempts.succeeded(username);

        if (request.getSession(false) != null) {
            request.changeSessionId();
        }
        var principal = ((UsuarioPrincipal) authenticated.getPrincipal()).withoutPassword();
        var context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(UsernamePasswordAuthenticationToken.authenticated(
                principal, null, principal.getAuthorities()));
        SecurityContextHolder.setContext(context);
        sessions.saveContext(context, request, response);
        csrfCookies.saveToken(csrfCookies.generateToken(request), request, response);
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(principal.profile());
    }
}
