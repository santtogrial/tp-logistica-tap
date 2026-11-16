package ar.edu.up.tap.logistica.tracking;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class NotificadorStomp implements NotificadorTiempoReal {

    private static final Logger log = LoggerFactory.getLogger(NotificadorStomp.class);

    public static final String TOPICO_VEHICULOS = "/topic/vehiculos";
    public static final String PREFIJO_SEGUIMIENTO = "/topic/seguimiento/";

    private final SimpMessagingTemplate mensajeria;

    public NotificadorStomp(SimpMessagingTemplate mensajeria) {
        this.mensajeria = mensajeria;
    }

    @Override
    public void enviarPosicion(PosicionVehiculo posicion) {
        enviar(TOPICO_VEHICULOS, posicion);
    }

    @Override
    public void enviarSeguimiento(MensajeSeguimiento mensaje) {
        enviar(PREFIJO_SEGUIMIENTO + mensaje.codigo(), mensaje);
    }

    // Un error al notificar no tiene que hacer fallar la operación de negocio que ya se guardó
    private void enviar(String destino, Object contenido) {
        try {
            mensajeria.convertAndSend(destino, contenido);
        } catch (Exception e) {
            log.warn("No se pudo enviar la notificación a {}: {}", destino, e.getMessage());
        }
    }
}
