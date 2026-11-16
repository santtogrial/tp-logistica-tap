package ar.edu.up.tap.logistica.tracking;

import ar.edu.up.tap.logistica.dominio.EstadoOrden;

import java.time.Instant;

/** Lo que recibe el destinatario por WebSocket. No incluye datos personales. */
public record MensajeSeguimiento(String tipo, String codigo, EstadoOrden estado, Double lat, Double lng, Instant instante) {

    public static MensajeSeguimiento cambioDeEstado(String codigo, EstadoOrden estado) {
        return new MensajeSeguimiento("ESTADO", codigo, estado, null, null, Instant.now());
    }

    public static MensajeSeguimiento posicion(String codigo, PosicionVehiculo posicion) {
        return new MensajeSeguimiento("POSICION", codigo, EstadoOrden.EN_TRANSITO,
                posicion.lat(), posicion.lng(), posicion.instante());
    }
}
