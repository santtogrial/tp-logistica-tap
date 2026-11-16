package ar.edu.up.tap.logistica.servicio;

import ar.edu.up.tap.logistica.dominio.Usuario;
import ar.edu.up.tap.logistica.repositorio.UsuarioRepository;
import ar.edu.up.tap.logistica.seguridad.JwtService;
import ar.edu.up.tap.logistica.web.dto.LoginRequest;
import ar.edu.up.tap.logistica.web.dto.LoginResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final AuthenticationManager authenticationManager;
    private final UsuarioRepository usuarioRepository;
    private final JwtService jwtService;

    public AuthService(AuthenticationManager authenticationManager, UsuarioRepository usuarioRepository,
                       JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.usuarioRepository = usuarioRepository;
        this.jwtService = jwtService;
    }

    public LoginResponse login(LoginRequest request) {
        // Valida email y contraseña contra el hash BCrypt; si no coinciden lanza BadCredentialsException
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password()));

        Usuario usuario = usuarioRepository.findByEmail(request.email())
                .orElseThrow(() -> new BadCredentialsException("Credenciales inválidas"));
        log.info("Inicio de sesión de {} ({})", usuario.getEmail(), usuario.getRol());
        return new LoginResponse(jwtService.generarToken(usuario), "Bearer", jwtService.getExpiracionMinutos(),
                usuario.getNombre(), usuario.getRol());
    }
}
