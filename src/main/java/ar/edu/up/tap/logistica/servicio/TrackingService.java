package ar.edu.up.tap.logistica.servicio;

import ar.edu.up.tap.logistica.dominio.Parada;
import ar.edu.up.tap.logistica.dominio.Ruta;
import ar.edu.up.tap.logistica.dominio.Usuario;
import ar.edu.up.tap.logistica.dominio.Vehiculo;
import ar.edu.up.tap.logistica.excepcion.RecursoNoEncontradoException;
import ar.edu.up.tap.logistica.excepcion.ReglaNegocioException;
import ar.edu.up.tap.logistica.repositorio.RutaRepository;
import ar.edu.up.tap.logistica.repositorio.VehiculoRepository;
import ar.edu.up.tap.logistica.seguridad.UsuarioActual;
import ar.edu.up.tap.logistica.tracking.MensajeSeguimiento;
import ar.edu.up.tap.logistica.tracking.NotificadorTiempoReal;
import ar.edu.up.tap.logistica.tracking.PosicionVehiculo;
import ar.edu.up.tap.logistica.tracking.RegistroPosiciones;
import ar.edu.up.tap.logistica.web.dto.PosicionRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class TrackingService {

    private final VehiculoRepository vehiculoRepository;
    private final RutaRepository rutaRepository;
    private final RegistroPosiciones registroPosiciones;
    private final NotificadorTiempoReal notificador;
    private final UsuarioActual usuarioActual;

    public TrackingService(VehiculoRepository vehiculoRepository, RutaRepository rutaRepository,
                           RegistroPosiciones registroPosiciones, NotificadorTiempoReal notificador,
                           UsuarioActual usuarioActual) {
        this.vehiculoRepository = vehiculoRepository;
        this.rutaRepository = rutaRepository;
        this.registroPosiciones = registroPosiciones;
        this.notificador = notificador;
        this.usuarioActual = usuarioActual;
    }

    /**
     * El chofer informa dónde está. La posición no se guarda en la base: se guarda la última
     * en memoria y se reenvía por WebSocket al operador y a los destinatarios que faltan visitar.
     */
    @Transactional(readOnly = true)
    public PosicionVehiculo registrarPosicion(Long vehiculoId, PosicionRequest request) {
        Usuario chofer = usuarioActual.obtener();
        Vehiculo vehiculo = vehiculoRepository.findById(vehiculoId)
                .orElseThrow(() -> RecursoNoEncontradoException.de("Vehículo", vehiculoId));
        if (!vehiculo.esConducidoPor(chofer)) {
            throw new AccessDeniedException("Solo el chofer asignado puede informar la posición de " + vehiculo.getPatente());
        }
        Ruta ruta = rutaRepository.buscarRutaEnCurso(vehiculoId)
                .orElseThrow(() -> new ReglaNegocioException("El vehículo " + vehiculo.getPatente() + " no tiene una ruta en curso"));

        PosicionVehiculo posicion = new PosicionVehiculo(vehiculoId, vehiculo.getPatente(),
                request.lat(), request.lng(), Instant.now());
        registroPosiciones.guardar(posicion);
        notificador.enviarPosicion(posicion);
        ruta.getParadas().stream()
                .filter(p -> !p.estaResuelta())
                .map(Parada::getOrden)
                .forEach(orden -> notificador.enviarSeguimiento(
                        MensajeSeguimiento.posicion(orden.getCodigoSeguimiento(), posicion)));
        return posicion;
    }
}
