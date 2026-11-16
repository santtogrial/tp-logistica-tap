package ar.edu.up.tap.logistica.seguridad;

import io.jsonwebtoken.JwtException;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.security.Principal;

/**
 * Seguridad del canal WebSocket:
 * - /topic/seguimiento/{codigo} es público (lo usa el destinatario sin cuenta).
 * - /topic/vehiculos expone la posición de toda la flota, así que exige JWT de operador o admin.
 */
@Component
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private static final String PREFIJO = "Bearer ";

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    public WebSocketAuthInterceptor(JwtService jwtService, UserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null || accessor.getCommand() == null) {
            return message;
        }
        if (accessor.getCommand() == StompCommand.CONNECT) {
            autenticar(accessor);
        } else if (accessor.getCommand() == StompCommand.SUBSCRIBE) {
            validarSuscripcion(accessor.getDestination(), accessor.getUser());
        } else if (accessor.getCommand() == StompCommand.SEND) {
            throw new MessagingException("El canal WebSocket es solo de lectura");
        }
        return message;
    }

    private void autenticar(StompHeaderAccessor accessor) {
        String header = accessor.getFirstNativeHeader("Authorization");
        if (header == null || !header.startsWith(PREFIJO)) {
            return; // conexión anónima: solo va a poder suscribirse al seguimiento público
        }
        try {
            String email = jwtService.extraerEmail(header.substring(PREFIJO.length()));
            UserDetails usuario = userDetailsService.loadUserByUsername(email);
            accessor.setUser(new UsernamePasswordAuthenticationToken(usuario, null, usuario.getAuthorities()));
        } catch (JwtException | UsernameNotFoundException e) {
            throw new MessagingException("Token inválido");
        }
    }

    private void validarSuscripcion(String destino, Principal usuario) {
        if (destino == null) {
            throw new MessagingException("Falta el destino");
        }
        if (destino.startsWith("/topic/seguimiento/")) {
            return;
        }
        if (destino.equals("/topic/vehiculos") && esOperadorOAdmin(usuario)) {
            return;
        }
        throw new MessagingException("No tenés permiso para suscribirte a " + destino);
    }

    private boolean esOperadorOAdmin(Principal usuario) {
        return usuario instanceof Authentication auth && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_OPERADOR") || a.getAuthority().equals("ROLE_ADMIN"));
    }
}
