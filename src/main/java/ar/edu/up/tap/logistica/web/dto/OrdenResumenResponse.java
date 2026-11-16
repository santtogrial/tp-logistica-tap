package ar.edu.up.tap.logistica.web.dto;

import ar.edu.up.tap.logistica.dominio.EstadoOrden;
import ar.edu.up.tap.logistica.dominio.Orden;

import java.time.LocalDateTime;

public record OrdenResumenResponse(Long id, String codigoSeguimiento, EstadoOrden estado, Long depositoId,
                                   String deposito, String destinatario, int intentosFallidos, LocalDateTime creadaEn) {

    public static OrdenResumenResponse desde(Orden orden) {
        return new OrdenResumenResponse(orden.getId(), orden.getCodigoSeguimiento(), orden.getEstado(),
                orden.getDeposito().getId(), orden.getDeposito().getNombre(), orden.getDestinatario().getNombre(),
                orden.getIntentosFallidos(), orden.getCreadaEn());
    }
}
