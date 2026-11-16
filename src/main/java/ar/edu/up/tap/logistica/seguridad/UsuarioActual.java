package ar.edu.up.tap.logistica.seguridad;

import ar.edu.up.tap.logistica.dominio.Usuario;
import ar.edu.up.tap.logistica.repositorio.UsuarioRepository;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/** Da acceso al usuario logueado. Se inyecta en los servicios para no depender de SecurityContextHolder. */
@Component
public class UsuarioActual {

    private final UsuarioRepository usuarioRepository;

    public UsuarioActual(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public Usuario obtener() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof UsuarioAutenticado principal)) {
            throw new AuthenticationCredentialsNotFoundException("No hay un usuario autenticado");
        }
        return usuarioRepository.findById(principal.getId())
                .orElseThrow(() -> new AuthenticationCredentialsNotFoundException("El usuario ya no existe"));
    }
}
