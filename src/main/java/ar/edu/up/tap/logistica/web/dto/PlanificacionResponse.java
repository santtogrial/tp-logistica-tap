package ar.edu.up.tap.logistica.web.dto;

import ar.edu.up.tap.logistica.dominio.EstadoPlanificacion;
import ar.edu.up.tap.logistica.dominio.Planificacion;

import java.time.LocalDateTime;
import java.util.List;

public record PlanificacionResponse(Long id, Long depositoId, EstadoPlanificacion estado, LocalDateTime creadaEn,
                                    List<RutaResponse> rutas) {

    public static PlanificacionResponse desde(Planificacion p) {
        return new PlanificacionResponse(p.getId(), p.getDeposito().getId(), p.getEstado(), p.getCreadaEn(),
                p.getRutas().stream().map(RutaResponse::desde).toList());
    }
}
