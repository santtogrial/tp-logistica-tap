package ar.edu.up.tap.logistica.seguridad;

import ar.edu.up.tap.logistica.dominio.Rol;
import ar.edu.up.tap.logistica.dominio.Usuario;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/** Adapta Usuario a lo que espera Spring Security, sin mezclar el dominio con el framework. */
public class UsuarioAutenticado implements UserDetails {

    private final Long id;
    private final String email;
    private final String passwordHash;
    private final Rol rol;
    private final boolean activo;

    public UsuarioAutenticado(Usuario usuario) {
        this.id = usuario.getId();
        this.email = usuario.getEmail();
        this.passwordHash = usuario.getPasswordHash();
        this.rol = usuario.getRol();
        this.activo = usuario.isActivo();
    }

    public Long getId() {
        return id;
    }

    public Rol getRol() {
        return rol;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + rol.name()));
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isEnabled() {
        return activo;
    }
}
