package ar.edu.up.tap.logistica.servicio;

import ar.edu.up.tap.logistica.Fixtures;
import ar.edu.up.tap.logistica.dominio.*;
import ar.edu.up.tap.logistica.excepcion.RecursoNoEncontradoException;
import ar.edu.up.tap.logistica.repositorio.OrdenRepository;
import ar.edu.up.tap.logistica.repositorio.ParadaRepository;
import ar.edu.up.tap.logistica.tracking.PosicionVehiculo;
import ar.edu.up.tap.logistica.tracking.RegistroPosiciones;
import ar.edu.up.tap.logistica.web.dto.SeguimientoResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SeguimientoServiceTest {

    @Mock
    private OrdenRepository ordenRepository;
    @Mock
    private ParadaRepository paradaRepository;
    @Mock
    private RegistroPosiciones registroPosiciones;

    @InjectMocks
    private SeguimientoService seguimientoService;

    @Test
    void unCodigoInexistenteResponde404() {
        when(ordenRepository.findByCodigoSeguimiento("LG-NOEXISTE")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> seguimientoService.consultar("lg-noexiste"))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    void unaOrdenQueTodaviaNoSalioNoInformaPosicion() {
        when(ordenRepository.findByCodigoSeguimiento("LG-AAAA1111")).thenReturn(Optional.of(Fixtures.orden(1L, "LG-AAAA1111")));

        SeguimientoResponse respuesta = seguimientoService.consultar("LG-AAAA1111");

        assertThat(respuesta.estado()).isEqualTo(EstadoOrden.CREADA);
        assertThat(respuesta.enCamino()).isNull();
        assertThat(respuesta.historial()).hasSize(1);
    }

    @Test
    void unaOrdenEnTransitoInformaPosicionYCuantasParadasFaltanAntes() {
        Usuario chofer = Fixtures.chofer(3L);
        Vehiculo vehiculo = Fixtures.vehiculo(1L, chofer);
        Orden primera = Fixtures.orden(1L, "LG-AAAA1111");
        Orden segunda = Fixtures.orden(2L, "LG-BBBB2222");
        Ruta ruta = new Planificacion(Fixtures.deposito()).agregarRuta(vehiculo, 10, 30);
        for (Orden o : new Orden[]{primera, segunda}) {
            o.asignar(chofer);
            o.iniciarTransito(chofer);
        }
        ruta.agregarParada(primera, 1, 10);
        ruta.agregarParada(segunda, 2, 25);
        ruta.iniciar();

        when(ordenRepository.findByCodigoSeguimiento("LG-BBBB2222")).thenReturn(Optional.of(segunda));
        when(paradaRepository.findFirstByOrdenIdOrderByIdDesc(2L)).thenReturn(Optional.of(ruta.getParadas().get(1)));
        when(registroPosiciones.ultima(1L)).thenReturn(Optional.of(
                new PosicionVehiculo(1L, "AB123CD", -34.59, -58.43, Instant.now())));

        SeguimientoResponse respuesta = seguimientoService.consultar("LG-BBBB2222");

        assertThat(respuesta.enCamino()).isNotNull();
        assertThat(respuesta.enCamino().paradasAntes()).isEqualTo(1);
        assertThat(respuesta.enCamino().lat()).isEqualTo(-34.59);
        assertThat(respuesta.enCamino().etaMin()).isBetween(24, 25);
    }
}
