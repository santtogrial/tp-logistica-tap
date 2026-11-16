package ar.edu.up.tap.logistica.web.dto;

import ar.edu.up.tap.logistica.dominio.EstadoOrden;
import ar.edu.up.tap.logistica.dominio.EstadoRuta;
import ar.edu.up.tap.logistica.dominio.Parada;
import ar.edu.up.tap.logistica.dominio.ResultadoEntrega;
import ar.edu.up.tap.logistica.dominio.Ruta;

import java.time.LocalDateTime;
import java.util.List;

public record RutaResponse(Long id, Long planificacionId, Long vehiculoId, String patente, EstadoRuta estado,
                           double origenLat, double origenLng, double distanciaKm, int duracionMin,
                           LocalDateTime iniciadaEn, LocalDateTime finalizadaEn, List<ParadaResponse> paradas) {

    public record ParadaResponse(Long id, int secuencia, Long ordenId, String codigoSeguimiento, EstadoOrden estadoOrden,
                                 String destinatario, String direccion, double lat, double lng, int etaMin,
                                 ResultadoEntrega resultado, String motivo, LocalDateTime llegadaReal) {

        static ParadaResponse desde(Parada p) {
            var orden = p.getOrden();
            var destinatario = orden.getDestinatario();
            return new ParadaResponse(p.getId(), p.getSecuencia(), orden.getId(), orden.getCodigoSeguimiento(),
                    orden.getEstado(), destinatario.getNombre(), destinatario.getDireccion(),
                    destinatario.getUbicacion().getLat(), destinatario.getUbicacion().getLng(),
                    p.getEtaMin(), p.getResultado(), p.getMotivo(), p.getLlegadaReal());
        }
    }

    public static RutaResponse desde(Ruta r) {
        var origen = r.getPlanificacion().getDeposito().getUbicacion();
        return new RutaResponse(r.getId(), r.getPlanificacion().getId(), r.getVehiculo().getId(),
                r.getVehiculo().getPatente(), r.getEstado(), origen.getLat(), origen.getLng(),
                r.getDistanciaKm(), r.getDuracionMin(),
                r.getIniciadaEn(), r.getFinalizadaEn(),
                r.getParadas().stream().map(ParadaResponse::desde).toList());
    }
}
