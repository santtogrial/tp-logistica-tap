package ar.edu.up.tap.logistica.servicio;

import ar.edu.up.tap.logistica.dominio.EstadoOrden;
import ar.edu.up.tap.logistica.dominio.Orden;
import ar.edu.up.tap.logistica.dominio.Parada;
import ar.edu.up.tap.logistica.dominio.Ruta;
import ar.edu.up.tap.logistica.excepcion.RecursoNoEncontradoException;
import ar.edu.up.tap.logistica.repositorio.OrdenRepository;
import ar.edu.up.tap.logistica.repositorio.ParadaRepository;
import ar.edu.up.tap.logistica.tracking.PosicionVehiculo;
import ar.edu.up.tap.logistica.tracking.RegistroPosiciones;
import ar.edu.up.tap.logistica.web.dto.SeguimientoResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Service
public class SeguimientoService {

    private final OrdenRepository ordenRepository;
    private final ParadaRepository paradaRepository;
    private final RegistroPosiciones registroPosiciones;

    public SeguimientoService(OrdenRepository ordenRepository, ParadaRepository paradaRepository,
                              RegistroPosiciones registroPosiciones) {
        this.ordenRepository = ordenRepository;
        this.paradaRepository = paradaRepository;
        this.registroPosiciones = registroPosiciones;
    }

    @Transactional(readOnly = true)
    public SeguimientoResponse consultar(String codigo) {
        Orden orden = ordenRepository.findByCodigoSeguimiento(codigo.trim().toUpperCase(Locale.ROOT))
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe una orden con el código " + codigo));

        List<SeguimientoResponse.Hito> historial = orden.getEventos().stream()
                .map(e -> new SeguimientoResponse.Hito(e.getEstadoNuevo(), e.getFecha()))
                .toList();
        SeguimientoResponse.EnCamino enCamino = orden.getEstado() == EstadoOrden.EN_TRANSITO ? calcularEnCamino(orden) : null;
        return new SeguimientoResponse(orden.getCodigoSeguimiento(), orden.getEstado(), historial, enCamino);
    }

    private SeguimientoResponse.EnCamino calcularEnCamino(Orden orden) {
        Parada parada = paradaRepository.findFirstByOrdenIdOrderByIdDesc(orden.getId()).orElse(null);
        if (parada == null) {
            return null;
        }
        Ruta ruta = parada.getRuta();
        int paradasAntes = (int) ruta.getParadas().stream()
                .filter(p -> p.getSecuencia() < parada.getSecuencia() && !p.estaResuelta())
                .count();
        int transcurridos = ruta.getIniciadaEn() == null ? 0
                : (int) Duration.between(ruta.getIniciadaEn(), LocalDateTime.now()).toMinutes();
        int etaRestante = Math.max(0, parada.getEtaMin() - transcurridos);

        PosicionVehiculo posicion = registroPosiciones.ultima(ruta.getVehiculo().getId()).orElse(null);
        return new SeguimientoResponse.EnCamino(
                posicion != null ? posicion.lat() : null,
                posicion != null ? posicion.lng() : null,
                posicion != null ? posicion.instante() : null,
                paradasAntes, etaRestante);
    }
}
