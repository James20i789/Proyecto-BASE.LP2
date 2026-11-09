package pe.edu.upeu.lp2_gp2.Controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import pe.edu.upeu.lp2_gp2.dto.AuthResponse;
import pe.edu.upeu.lp2_gp2.dto.LoginRequest;
import pe.edu.upeu.lp2_gp2.dto.RegistroDTO;
import pe.edu.upeu.lp2_gp2.dto.UsuarioDTO;
import pe.edu.upeu.lp2_gp2.entity.Usuario;
import pe.edu.upeu.lp2_gp2.repository.UsuarioRepository;
import pe.edu.upeu.lp2_gp2.security.JwtService;
import pe.edu.upeu.lp2_gp2.security.UsuarioPrincipal;
import pe.edu.upeu.lp2_gp2.services.IUsuarioService;

@RequestMapping("/api/v1/auth")
public class AuthController {
    private final IUsuarioService usuarioService;
    private final AuthenticationManager authenticationManager;
    private final UsuarioRepository usuarioRepository;
    private final JwtService jwtService;

    public AuthController(IUsuarioService usuarioService, AuthenticationManager authenticationManager, UsuarioRepository usuarioRepository, JwtService jwtService) {
        this.usuarioService = usuarioService;
        this.authenticationManager = authenticationManager;
        this.usuarioRepository = usuarioRepository;
        this.jwtService = jwtService;
    }


    // PROCESO DE REGISTRO | POST MAPPING
    @PostMapping("/registro")
    public ResponseEntity<UsuarioDTO>register(@Valid@RequestBody RegistroDTO dto){
        return usuarioService.registrarCliente(dto)
                .map(user -> new ResponseEntity<>(user , HttpStatus.CREATED))
                .orElseThrow(() -> new RuntimeException("El email o nombre del usuario ya está en uso"));
    }
    //  PROCESO DE LOGIN | POST MAPPING
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request){
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.usuario(), request.password())
        );

        Usuario user = usuarioRepository.findByUsuario(request.usuario()).orElseThrow();
        String jwtToken = jwtService.generateToken(new UsuarioPrincipal(user));

        return  ResponseEntity.ok(new AuthResponse(
                jwtToken,
                user.getUsuario(),
                user.getRol().name(),
                user.getPersona().getIdPersona()));

    }
}
