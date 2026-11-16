package ar.edu.up.tap.logistica.servicio;

import ar.edu.up.tap.logistica.Fixtures;
import ar.edu.up.tap.logistica.dominio.*;
import ar.edu.up.tap.logistica.excepcion.ReglaNegocioException;
import ar.edu.up.tap.logistica.excepcion.TransicionInvalidaException;
import ar.edu.up.tap.logistica.repositorio.ParadaRepository;
import ar.edu.up.tap.logistica.repositorio.RutaRepository;
import ar.edu.up.tap.logistica.repositorio.VehiculoRepository;
import ar.edu.up.tap.logistica.seguridad.UsuarioActual;
import ar.edu.up.tap.logistica.tracking.MensajeSeguimiento;
import ar.edu.up.tap.logistica.tracking.NotificadorTiempoReal;
import ar.edu.up.tap.logistica.tracking.RegistroPosiciones;
import ar.edu.up.tap.logistica.web.dto.ResultadoParadaRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RutaServiceTest {

    @Mock
    private RutaRepository rutaRepository;
    @Mock
    private ParadaRepository paradaRepository;
    @Mock
    private VehiculoRepository vehiculoRepository;
    @Mock
    private StockService stockService;
    @Mock
    private RegistroPosiciones registroPosiciones;
    @Mock
    private NotificadorTiempoReal notificador;
    @Mock
    private UsuarioActual usuarioActual;

    @InjectMocks
    private RutaService rutaService;

    private final Usuario chofer = Fixtures.chofer(3L);
    private final Vehiculo vehiculo = Fixtures.vehiculo(1L, chofer);
    private Orden orden1;
    private Orden orden2;
    private Ruta ruta;

    @BeforeEach
    void setUp() {
        orden1 = Fixtures.orden(1L, "LG-AAAA1111");
        orden2 = Fixtures.orden(2L, "LG-BBBB2222");
        orden1.asignar(chofer);
        orden2.asignar(chofer);

        ruta = Fixtures.conId(new Planificacion(Fixtures.deposito()).agregarRuta(vehiculo, 12.5, 45), 10L);
        ruta.agregarParada(orden1, 1, 10);
        ruta.agregarParada(orden2, 2, 25);

        lenient().when(usuarioActual.obtener()).thenReturn(chofer);
        lenient().when(rutaRepository.findById(10L)).thenReturn(Optional.of(ruta));
    }

    @Test
    void iniciarPoneLasOrdenesEnTransitoYElVehiculoEnRuta() {
        rutaService.iniciar(10L);

        assertThat(ruta.getEstado()).isEqualTo(EstadoRuta.EN_CURSO);
        assertThat(vehiculo.getEstado()).isEqualTo(EstadoVehiculo.EN_RUTA);
        assertThat(orden1.getEstado()).isEqualTo(EstadoOrden.EN_TRANSITO);
        assertThat(orden2.getEstado()).isEqualTo(EstadoOrden.EN_TRANSITO);
        verify(notificador, times(2)).enviarSeguimiento(any(MensajeSeguimiento.class));
    }

    @Test
    void unChoferNoPuedeIniciarLaRutaDeOtroVehiculo() {
        when(usuarioActual.obtener()).thenReturn(Fixtures.chofer(4L));

        assertThatThrownBy(() -> rutaService.iniciar(10L)).isInstanceOf(AccessDeniedException.class);
        assertThat(ruta.getEstado()).isEqualTo(EstadoRuta.PLANIFICADA);
    }

    @Test
    void unaEntregaFallidaSinMotivoSeRechaza() {
        rutaService.iniciar(10L);
        Parada parada = ruta.getParadas().get(0);
        Fixtures.conId(parada, 100L);
        when(paradaRepository.findById(100L)).thenReturn(Optional.of(parada));

        assertThatThrownBy(() -> rutaService.registrarResultado(100L,
                new ResultadoParadaRequest(ResultadoEntrega.NO_ENTREGADA, " ")))
                .isInstanceOf(ReglaNegocioException.class);
        assertThat(orden1.getEstado()).isEqualTo(EstadoOrden.EN_TRANSITO);
    }

    @Test
    void noSePuedeFinalizarConParadasPendientes() {
        rutaService.iniciar(10L);

        assertThatThrownBy(() -> rutaService.finalizar(10L)).isInstanceOf(TransicionInvalidaException.class);
    }

    @Test
    void alFinalizarUnaOrdenConSuPrimerIntentoFallidoQuedaParaReplanificar() {
        rutaService.iniciar(10L);
        registrar(0, ResultadoEntrega.ENTREGADA, null);
        registrar(1, ResultadoEntrega.NO_ENTREGADA, "Cliente ausente");

        rutaService.finalizar(10L);

        assertThat(orden1.getEstado()).isEqualTo(EstadoOrden.ENTREGADA);
        assertThat(orden2.getEstado()).isEqualTo(EstadoOrden.NO_ENTREGADA);
        assertThat(orden2.puedePlanificarse()).isTrue();
        assertThat(vehiculo.getEstado()).isEqualTo(EstadoVehiculo.DISPONIBLE);
        verify(stockService, never()).reintegrar(any());
        verify(registroPosiciones).eliminar(1L);
    }

    @Test
    void alFinalizarUnaOrdenConDosIntentosFallidosSeDevuelveYSeReintegraElStock() {
        // Esta orden ya tuvo un intento fallido en una ruta anterior
        Orden reintento = Fixtures.orden(3L, "LG-CCCC3333");
        reintento.asignar(chofer);
        reintento.iniciarTransito(chofer);
        reintento.marcarNoEntregada("Cliente ausente", chofer);
        reintento.asignar(chofer);
        Ruta segundaRuta = Fixtures.conId(new Planificacion(Fixtures.deposito()).agregarRuta(vehiculo, 5, 20), 11L);
        segundaRuta.agregarParada(reintento, 1, 10);
        when(rutaRepository.findById(11L)).thenReturn(Optional.of(segundaRuta));

        rutaService.iniciar(11L);
        Parada parada = Fixtures.conId(segundaRuta.getParadas().get(0), 200L);
        when(paradaRepository.findById(200L)).thenReturn(Optional.of(parada));
        rutaService.registrarResultado(200L, new ResultadoParadaRequest(ResultadoEntrega.NO_ENTREGADA, "Dirección cerrada"));
        rutaService.finalizar(11L);

        assertThat(reintento.getEstado()).isEqualTo(EstadoOrden.DEVUELTA);
        verify(stockService).reintegrar(reintento);
    }

    @Test
    void anularUnaRutaPlanificadaLiberaLasOrdenes() {
        when(usuarioActual.obtener()).thenReturn(Fixtures.operador());

        rutaService.anular(10L);

        assertThat(ruta.getEstado()).isEqualTo(EstadoRuta.ANULADA);
        assertThat(orden1.getEstado()).isEqualTo(EstadoOrden.CREADA);
        assertThat(orden2.getEstado()).isEqualTo(EstadoOrden.CREADA);
    }

    private void registrar(int indice, ResultadoEntrega resultado, String motivo) {
        Parada parada = Fixtures.conId(ruta.getParadas().get(indice), 100L + indice);
        when(paradaRepository.findById(100L + indice)).thenReturn(Optional.of(parada));
        rutaService.registrarResultado(100L + indice, new ResultadoParadaRequest(resultado, motivo));
    }
}
