package ar.edu.up.tap.logistica.web.dto;

import ar.edu.up.tap.logistica.dominio.EstadoOrden;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

/** Respuesta pública: estado e historial, sin nombre, teléfono ni dirección del destinatario. */
public record SeguimientoResponse(String codigo, EstadoOrden estado, List<Hito> historial, EnCamino enCamino) {

    public record Hito(EstadoOrden estado, LocalDateTime fecha) {
    }

    /** Solo viene cuando la orden está en tránsito. */
    public record EnCamino(Double lat, Double lng, Instant actualizado, int paradasAntes, int etaMin) {
    }
}
