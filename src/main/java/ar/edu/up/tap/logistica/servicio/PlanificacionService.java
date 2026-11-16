package ar.edu.up.tap.logistica.servicio;

import ar.edu.up.tap.logistica.dominio.Deposito;
import ar.edu.up.tap.logistica.dominio.EstadoRuta;
import ar.edu.up.tap.logistica.dominio.Orden;
import ar.edu.up.tap.logistica.dominio.Planificacion;
import ar.edu.up.tap.logistica.dominio.Ruta;
import ar.edu.up.tap.logistica.dominio.Usuario;
import ar.edu.up.tap.logistica.dominio.Vehiculo;
import ar.edu.up.tap.logistica.excepcion.RecursoNoEncontradoException;
import ar.edu.up.tap.logistica.excepcion.ReglaNegocioException;
import ar.edu.up.tap.logistica.optimizacion.OptimizadorRutas;
import ar.edu.up.tap.logistica.optimizacion.ParadaPlanificada;
import ar.edu.up.tap.logistica.optimizacion.PuntoEntrega;
import ar.edu.up.tap.logistica.optimizacion.RecorridoPlanificado;
import ar.edu.up.tap.logistica.repositorio.DepositoRepository;
import ar.edu.up.tap.logistica.repositorio.OrdenRepository;
import ar.edu.up.tap.logistica.repositorio.PlanificacionRepository;
import ar.edu.up.tap.logistica.repositorio.RutaRepository;
import ar.edu.up.tap.logistica.repositorio.VehiculoRepository;
import ar.edu.up.tap.logistica.seguridad.UsuarioActual;
import ar.edu.up.tap.logistica.web.dto.PlanificacionRequest;
import ar.edu.up.tap.logistica.web.dto.PlanificacionResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class PlanificacionService {

    private static final Logger log = LoggerFactory.getLogger(PlanificacionService.class);
    private static final List<EstadoRuta> RUTAS_PENDIENTES = List.of(EstadoRuta.PLANIFICADA, EstadoRuta.EN_CURSO);

    private final PlanificacionRepository planificacionRepository;
    private final DepositoRepository depositoRepository;
    private final VehiculoRepository vehiculoRepository;
    private final OrdenRepository ordenRepository;
    private final RutaRepository rutaRepository;
    private final OptimizadorRutas optimizador;
    private final UsuarioActual usuarioActual;

    public PlanificacionService(PlanificacionRepository planificacionRepository, DepositoRepository depositoRepository,
                                VehiculoRepository vehiculoRepository, OrdenRepository ordenRepository,
                                RutaRepository rutaRepository, OptimizadorRutas optimizador,
                                UsuarioActual usuarioActual) {
        this.planificacionRepository = planificacionRepository;
        this.depositoRepository = depositoRepository;
        this.vehiculoRepository = vehiculoRepository;
        this.ordenRepository = ordenRepository;
        this.rutaRepository = rutaRepository;
        this.optimizador = optimizador;
        this.usuarioActual = usuarioActual;
    }

    @Transactional
    public PlanificacionResponse planificar(PlanificacionRequest request) {
        Usuario usuario = usuarioActual.obtener();
        Deposito deposito = depositoRepository.findById(request.depositoId())
                .orElseThrow(() -> RecursoNoEncontradoException.de("Depósito", request.depositoId()));
        List<Vehiculo> vehiculos = buscarVehiculos(request.vehiculoIds(), deposito);
        List<Orden> ordenes = buscarOrdenes(request.ordenIds(), deposito);

        List<PuntoEntrega> puntos = ordenes.stream()
                .map(o -> new PuntoEntrega(o.getId(), o.getDestinatario().getUbicacion()))
                .toList();
        List<RecorridoPlanificado> recorridos = optimizador.optimizar(
                deposito.getUbicacion(), vehiculos.stream().map(Vehiculo::getId).toList(), puntos);

        Map<Long, Vehiculo> vehiculosPorId = porId(vehiculos, Vehiculo::getId);
        Map<Long, Orden> ordenesPorId = porId(ordenes, Orden::getId);

        Planificacion planificacion = new Planificacion(deposito);
        for (RecorridoPlanificado recorrido : recorridos) {
            Ruta ruta = planificacion.agregarRuta(vehiculosPorId.get(recorrido.vehiculoId()),
                    recorrido.distanciaKm(), recorrido.duracionMin());
            for (ParadaPlanificada parada : recorrido.paradas()) {
                Orden orden = ordenesPorId.get(parada.ordenId());
                orden.asignar(usuario);
                ruta.agregarParada(orden, parada.secuencia(), parada.etaMin());
            }
        }
        planificacion.completar();
        planificacionRepository.save(planificacion);

        log.info("Planificación {}: {} órdenes en {} rutas desde {}",
                planificacion.getId(), ordenes.size(), recorridos.size(), deposito.getNombre());
        return PlanificacionResponse.desde(planificacion);
    }

    @Transactional(readOnly = true)
    public PlanificacionResponse obtener(Long id) {
        return planificacionRepository.findById(id)
                .map(PlanificacionResponse::desde)
                .orElseThrow(() -> RecursoNoEncontradoException.de("Planificación", id));
    }

    private List<Vehiculo> buscarVehiculos(List<Long> ids, Deposito deposito) {
        List<Long> distintos = ids.stream().distinct().toList();
        List<Vehiculo> vehiculos = vehiculoRepository.findAllById(distintos);
        validarQueExistan(distintos, vehiculos.stream().map(Vehiculo::getId).toList(), "Vehículo");

        for (Vehiculo v : vehiculos) {
            if (!v.getDepositoBase().getId().equals(deposito.getId())) {
                throw new ReglaNegocioException("El vehículo " + v.getPatente() + " tiene base en otro depósito");
            }
            if (!v.estaDisponible()) {
                throw new ReglaNegocioException("El vehículo " + v.getPatente() + " no está disponible (" + v.getEstado() + ")");
            }
            if (v.getChofer() == null) {
                throw new ReglaNegocioException("El vehículo " + v.getPatente() + " no tiene chofer asignado");
            }
            if (rutaRepository.findFirstByVehiculoIdAndEstadoInOrderByIdDesc(v.getId(), RUTAS_PENDIENTES).isPresent()) {
                throw new ReglaNegocioException("El vehículo " + v.getPatente() + " ya tiene una ruta pendiente");
            }
        }
        return vehiculos;
    }

    private List<Orden> buscarOrdenes(List<Long> ids, Deposito deposito) {
        List<Orden> ordenes;
        if (ids == null || ids.isEmpty()) {
            ordenes = ordenRepository.buscarPlanificables(deposito.getId());
        } else {
            List<Long> distintos = ids.stream().distinct().toList();
            ordenes = ordenRepository.findAllById(distintos);
            validarQueExistan(distintos, ordenes.stream().map(Orden::getId).toList(), "Orden");
            for (Orden o : ordenes) {
                if (!o.getDeposito().getId().equals(deposito.getId())) {
                    throw new ReglaNegocioException("La orden " + o.getCodigoSeguimiento() + " sale de otro depósito");
                }
                if (!o.puedePlanificarse()) {
                    throw new ReglaNegocioException(
                            "La orden " + o.getCodigoSeguimiento() + " está " + o.getEstado() + " y no se puede planificar");
                }
            }
        }
        if (ordenes.isEmpty()) {
            throw new ReglaNegocioException("No hay órdenes pendientes para planificar en " + deposito.getNombre());
        }
        return ordenes;
    }

    private void validarQueExistan(List<Long> pedidos, List<Long> encontrados, String recurso) {
        pedidos.stream()
                .filter(id -> !encontrados.contains(id))
                .findFirst()
                .ifPresent(id -> {
                    throw RecursoNoEncontradoException.de(recurso, id);
                });
    }

    private <T> Map<Long, T> porId(List<T> elementos, Function<T, Long> id) {
        return elementos.stream().collect(Collectors.toMap(id, Function.identity()));
    }
}
