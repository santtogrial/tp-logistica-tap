package ar.edu.up.tap.logistica.servicio;

import ar.edu.up.tap.logistica.Fixtures;
import ar.edu.up.tap.logistica.dominio.*;
import ar.edu.up.tap.logistica.excepcion.ReglaNegocioException;
import ar.edu.up.tap.logistica.repositorio.RutaRepository;
import ar.edu.up.tap.logistica.repositorio.VehiculoRepository;
import ar.edu.up.tap.logistica.seguridad.UsuarioActual;
import ar.edu.up.tap.logistica.tracking.MensajeSeguimiento;
import ar.edu.up.tap.logistica.tracking.NotificadorTiempoReal;
import ar.edu.up.tap.logistica.tracking.PosicionVehiculo;
import ar.edu.up.tap.logistica.tracking.RegistroPosiciones;
import ar.edu.up.tap.logistica.web.dto.PosicionRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
class TrackingServiceTest {

    @Mock
    private VehiculoRepository vehiculoRepository;
    @Mock
    private RutaRepository rutaRepository;
    @Mock
    private RegistroPosiciones registroPosiciones;
    @Mock
    private NotificadorTiempoReal notificador;
    @Mock
    private UsuarioActual usuarioActual;

    @InjectMocks
    private TrackingService trackingService;

    private final Usuario chofer = Fixtures.chofer(3L);
    private final Vehiculo vehiculo = Fixtures.vehiculo(1L, chofer);

    @BeforeEach
    void setUp() {
        lenient().when(usuarioActual.obtener()).thenReturn(chofer);
        lenient().when(vehiculoRepository.findById(1L)).thenReturn(Optional.of(vehiculo));
    }

    @Test
    void laPosicionSeGuardaYSeAvisaALaFlotaYACadaPedidoPendiente() {
        Orden entregada = Fixtures.orden(1L, "LG-AAAA1111");
        Orden pendiente = Fixtures.orden(2L, "LG-BBBB2222");
        Ruta ruta = new Planificacion(Fixtures.deposito()).agregarRuta(vehiculo, 10, 30);
        ruta.agregarParada(entregada, 1, 10);
        ruta.agregarParada(pendiente, 2, 20);
        ruta.getParadas().get(0).registrarResultado(ResultadoEntrega.ENTREGADA, null);
        when(rutaRepository.buscarRutaEnCurso(1L)).thenReturn(Optional.of(ruta));

        trackingService.registrarPosicion(1L, new PosicionRequest(-34.59, -58.43));

        verify(registroPosiciones).guardar(any(PosicionVehiculo.class));
        verify(notificador).enviarPosicion(any(PosicionVehiculo.class));
        ArgumentCaptor<MensajeSeguimiento> mensaje = ArgumentCaptor.forClass(MensajeSeguimiento.class);
        verify(notificador, times(1)).enviarSeguimiento(mensaje.capture());
        assertThat(mensaje.getValue().codigo()).isEqualTo("LG-BBBB2222");
        assertThat(mensaje.getValue().lat()).isEqualTo(-34.59);
    }

    @Test
    void otroChoferNoPuedeInformarLaPosicionDelVehiculo() {
        when(usuarioActual.obtener()).thenReturn(Fixtures.chofer(4L));

        assertThatThrownBy(() -> trackingService.registrarPosicion(1L, new PosicionRequest(-34.59, -58.43)))
                .isInstanceOf(AccessDeniedException.class);
        verifyNoInteractions(registroPosiciones, notificador);
    }

    @Test
    void sinRutaEnCursoNoSeAceptaLaPosicion() {
        when(rutaRepository.buscarRutaEnCurso(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> trackingService.registrarPosicion(1L, new PosicionRequest(-34.59, -58.43)))
                .isInstanceOf(ReglaNegocioException.class);
        verifyNoInteractions(registroPosiciones);
    }
}
