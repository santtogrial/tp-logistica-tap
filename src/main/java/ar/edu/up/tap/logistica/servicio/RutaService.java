package ar.edu.up.tap.logistica.servicio;

import ar.edu.up.tap.logistica.dominio.EstadoOrden;
import ar.edu.up.tap.logistica.dominio.EstadoRuta;
import ar.edu.up.tap.logistica.dominio.Orden;
import ar.edu.up.tap.logistica.dominio.Parada;
import ar.edu.up.tap.logistica.dominio.ResultadoEntrega;
import ar.edu.up.tap.logistica.dominio.Ruta;
import ar.edu.up.tap.logistica.dominio.Usuario;
import ar.edu.up.tap.logistica.dominio.Vehiculo;
import ar.edu.up.tap.logistica.excepcion.RecursoNoEncontradoException;
import ar.edu.up.tap.logistica.excepcion.TransicionInvalidaException;
import ar.edu.up.tap.logistica.repositorio.ParadaRepository;
import ar.edu.up.tap.logistica.repositorio.RutaRepository;
import ar.edu.up.tap.logistica.repositorio.VehiculoRepository;
import ar.edu.up.tap.logistica.seguridad.UsuarioActual;
import ar.edu.up.tap.logistica.tracking.MensajeSeguimiento;
import ar.edu.up.tap.logistica.tracking.NotificadorTiempoReal;
import ar.edu.up.tap.logistica.tracking.RegistroPosiciones;
import ar.edu.up.tap.logistica.web.dto.PaginaResponse;
import ar.edu.up.tap.logistica.web.dto.ResultadoParadaRequest;
import ar.edu.up.tap.logistica.web.dto.RutaResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RutaService {

    private static final Logger log = LoggerFactory.getLogger(RutaService.class);

    private final RutaRepository rutaRepository;
    private final ParadaRepository paradaRepository;
    private final VehiculoRepository vehiculoRepository;
    private final StockService stockService;
    private final RegistroPosiciones registroPosiciones;
    private final NotificadorTiempoReal notificador;
    private final UsuarioActual usuarioActual;

    public RutaService(RutaRepository rutaRepository, ParadaRepository paradaRepository,
                       VehiculoRepository vehiculoRepository, StockService stockService,
                       RegistroPosiciones registroPosiciones, NotificadorTiempoReal notificador,
                       UsuarioActual usuarioActual) {
        this.rutaRepository = rutaRepository;
        this.paradaRepository = paradaRepository;
        this.vehiculoRepository = vehiculoRepository;
        this.stockService = stockService;
        this.registroPosiciones = registroPosiciones;
        this.notificador = notificador;
        this.usuarioActual = usuarioActual;
    }

    @Transactional(readOnly = true)
    public PaginaResponse<RutaResponse> listar(EstadoRuta estado, Pageable pageable) {
        var pagina = estado == null ? rutaRepository.findAll(pageable) : rutaRepository.findByEstado(estado, pageable);
        return PaginaResponse.de(pagina.map(RutaResponse::desde));
    }

    @Transactional(readOnly = true)
    public RutaResponse obtener(Long id) {
        return RutaResponse.desde(buscar(id));
    }

    @Transactional(readOnly = true)
    public RutaResponse rutaActual() {
        Usuario chofer = usuarioActual.obtener();
        Vehiculo vehiculo = vehiculoRepository.findByChoferId(chofer.getId())
                .orElseThrow(() -> new RecursoNoEncontradoException("No tenés un vehículo asignado"));
        return rutaRepository.findFirstByVehiculoIdAndEstadoInOrderByIdDesc(
                        vehiculo.getId(), List.of(EstadoRuta.EN_CURSO, EstadoRuta.PLANIFICADA))
                .map(RutaResponse::desde)
                .orElseThrow(() -> new RecursoNoEncontradoException("No tenés rutas pendientes"));
    }

    @Transactional
    public RutaResponse iniciar(Long id) {
        Usuario usuario = usuarioActual.obtener();
        Ruta ruta = buscar(id);
        verificarAcceso(ruta, usuario);

        ruta.iniciar();
        ruta.getVehiculo().salirARuta();
        for (Parada parada : ruta.getParadas()) {
            parada.getOrden().iniciarTransito(usuario);
            notificarEstado(parada.getOrden());
        }
        log.info("Ruta {} iniciada por {} ({} paradas)", id, usuario.getEmail(), ruta.getParadas().size());
        return RutaResponse.desde(ruta);
    }

    @Transactional
    public RutaResponse registrarResultado(Long paradaId, ResultadoParadaRequest request) {
        Usuario usuario = usuarioActual.obtener();
        Parada parada = paradaRepository.findById(paradaId)
                .orElseThrow(() -> RecursoNoEncontradoException.de("Parada", paradaId));
        Ruta ruta = parada.getRuta();
        verificarAcceso(ruta, usuario);
        if (!ruta.estaEnCurso()) {
            throw new TransicionInvalidaException("La ruta " + ruta.getId() + " no está en curso");
        }

        parada.registrarResultado(request.resultado(), request.motivo());
        Orden orden = parada.getOrden();
        if (request.resultado() == ResultadoEntrega.ENTREGADA) {
            orden.marcarEntregada(usuario);
        } else {
            orden.marcarNoEntregada(request.motivo(), usuario);
        }
        notificarEstado(orden);
        log.info("Parada {} de la ruta {}: {}", paradaId, ruta.getId(), request.resultado());
        return RutaResponse.desde(ruta);
    }

    @Transactional
    public RutaResponse finalizar(Long id) {
        Usuario usuario = usuarioActual.obtener();
        Ruta ruta = buscar(id);
        verificarAcceso(ruta, usuario);

        ruta.finalizar();
        ruta.getVehiculo().volverDeRuta();
        registroPosiciones.eliminar(ruta.getVehiculo().getId());

        // Las que no se entregaron vuelven con el vehículo. Si ya agotaron los intentos se cierran como devueltas.
        for (Parada parada : ruta.getParadas()) {
            Orden orden = parada.getOrden();
            if (orden.getEstado() == EstadoOrden.NO_ENTREGADA && orden.agotoIntentos()) {
                orden.devolver(usuario);
                stockService.reintegrar(orden);
                notificarEstado(orden);
            }
        }
        log.info("Ruta {} finalizada", id);
        return RutaResponse.desde(ruta);
    }

    @Transactional
    public RutaResponse anular(Long id) {
        Usuario usuario = usuarioActual.obtener();
        Ruta ruta = buscar(id);
        ruta.anular();
        for (Parada parada : ruta.getParadas()) {
            parada.getOrden().desasignar(usuario);
        }
        log.info("Ruta {} anulada por {}", id, usuario.getEmail());
        return RutaResponse.desde(ruta);
    }

    private Ruta buscar(Long id) {
        return rutaRepository.findById(id).orElseThrow(() -> RecursoNoEncontradoException.de("Ruta", id));
    }

    private void verificarAcceso(Ruta ruta, Usuario usuario) {
        if (usuario.esChofer() && !ruta.getVehiculo().esConducidoPor(usuario)) {
            throw new AccessDeniedException("La ruta " + ruta.getId() + " no corresponde a tu vehículo");
        }
    }

    private void notificarEstado(Orden orden) {
        notificador.enviarSeguimiento(MensajeSeguimiento.cambioDeEstado(orden.getCodigoSeguimiento(), orden.getEstado()));
    }
}
