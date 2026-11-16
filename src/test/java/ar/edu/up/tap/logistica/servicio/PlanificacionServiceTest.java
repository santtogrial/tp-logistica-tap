package ar.edu.up.tap.logistica.servicio;

import ar.edu.up.tap.logistica.Fixtures;
import ar.edu.up.tap.logistica.dominio.*;
import ar.edu.up.tap.logistica.excepcion.ReglaNegocioException;
import ar.edu.up.tap.logistica.optimizacion.OptimizadorRutas;
import ar.edu.up.tap.logistica.optimizacion.ParadaPlanificada;
import ar.edu.up.tap.logistica.optimizacion.RecorridoPlanificado;
import ar.edu.up.tap.logistica.repositorio.*;
import ar.edu.up.tap.logistica.seguridad.UsuarioActual;
import ar.edu.up.tap.logistica.web.dto.PlanificacionRequest;
import ar.edu.up.tap.logistica.web.dto.PlanificacionResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * El optimizador se reemplaza por un mock: acá se prueba la orquestación de la planificación,
 * no el algoritmo (que tiene su propio test). Es lo que permite la inyección de dependencias.
 */
@ExtendWith(MockitoExtension.class)
class PlanificacionServiceTest {

    @Mock
    private PlanificacionRepository planificacionRepository;
    @Mock
    private DepositoRepository depositoRepository;
    @Mock
    private VehiculoRepository vehiculoRepository;
    @Mock
    private OrdenRepository ordenRepository;
    @Mock
    private RutaRepository rutaRepository;
    @Mock
    private OptimizadorRutas optimizador;
    @Mock
    private UsuarioActual usuarioActual;

    @InjectMocks
    private PlanificacionService planificacionService;

    private final Deposito deposito = Fixtures.deposito();
    private final Vehiculo vehiculo = Fixtures.vehiculo(7L, Fixtures.chofer(3L));
    private Orden orden1;
    private Orden orden2;

    @BeforeEach
    void setUp() {
        orden1 = Fixtures.orden(1L, "LG-AAAA1111");
        orden2 = Fixtures.orden(2L, "LG-BBBB2222");
        lenient().when(usuarioActual.obtener()).thenReturn(Fixtures.operador());
        lenient().when(depositoRepository.findById(1L)).thenReturn(Optional.of(deposito));
    }

    @Test
    void planificarAsignaLasOrdenesEnElOrdenQueDevuelveElOptimizador() {
        when(vehiculoRepository.findAllById(List.of(7L))).thenReturn(List.of(vehiculo));
        when(ordenRepository.buscarPlanificables(1L)).thenReturn(List.of(orden1, orden2));
        when(optimizador.optimizar(any(), eq(List.of(7L)), anyList())).thenReturn(List.of(
                new RecorridoPlanificado(7L, List.of(new ParadaPlanificada(2L, 1, 8), new ParadaPlanificada(1L, 2, 20)), 9.3, 35)));

        PlanificacionResponse respuesta = planificacionService.planificar(new PlanificacionRequest(1L, List.of(7L), null));

        assertThat(respuesta.estado()).isEqualTo(EstadoPlanificacion.COMPLETADA);
        assertThat(respuesta.rutas()).singleElement().satisfies(ruta ->
                assertThat(ruta.paradas()).extracting(p -> p.codigoSeguimiento())
                        .containsExactly("LG-BBBB2222", "LG-AAAA1111"));
        assertThat(orden1.getEstado()).isEqualTo(EstadoOrden.ASIGNADA);
        assertThat(orden2.getEstado()).isEqualTo(EstadoOrden.ASIGNADA);
        verify(planificacionRepository).save(any(Planificacion.class));
    }

    @Test
    void unVehiculoEnTallerNoSePuedePlanificar() {
        vehiculo.cambiarEstado(EstadoVehiculo.EN_TALLER);
        when(vehiculoRepository.findAllById(List.of(7L))).thenReturn(List.of(vehiculo));

        assertThatThrownBy(() -> planificacionService.planificar(new PlanificacionRequest(1L, List.of(7L), null)))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("no está disponible");
        verifyNoInteractions(optimizador);
    }

    @Test
    void sinOrdenesPendientesNoSeLlamaAlOptimizador() {
        when(vehiculoRepository.findAllById(List.of(7L))).thenReturn(List.of(vehiculo));
        when(ordenRepository.buscarPlanificables(1L)).thenReturn(List.of());

        assertThatThrownBy(() -> planificacionService.planificar(new PlanificacionRequest(1L, List.of(7L), null)))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("No hay órdenes");
        verifyNoInteractions(optimizador);
    }

    @Test
    void unaOrdenYaEntregadaNoSePuedeVolverAPlanificar() {
        Usuario chofer = Fixtures.chofer(3L);
        orden1.asignar(chofer);
        orden1.iniciarTransito(chofer);
        orden1.marcarEntregada(chofer);
        when(vehiculoRepository.findAllById(List.of(7L))).thenReturn(List.of(vehiculo));
        when(ordenRepository.findAllById(List.of(1L))).thenReturn(List.of(orden1));

        assertThatThrownBy(() -> planificacionService.planificar(new PlanificacionRequest(1L, List.of(7L), List.of(1L))))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("ENTREGADA");
    }
}
