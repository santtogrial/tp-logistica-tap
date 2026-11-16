package ar.edu.up.tap.logistica.dominio;

import ar.edu.up.tap.logistica.Fixtures;
import ar.edu.up.tap.logistica.excepcion.TransicionInvalidaException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrdenTest {

    private final Usuario chofer = Fixtures.chofer(3L);

    @Test
    void unaOrdenNuevaQuedaCreadaConSuPrimerEvento() {
        Orden orden = Fixtures.orden(1L, "LG-AAAA1111");

        assertThat(orden.getEstado()).isEqualTo(EstadoOrden.CREADA);
        assertThat(orden.getEventos()).hasSize(1);
        assertThat(orden.getEventos().get(0).getEstadoNuevo()).isEqualTo(EstadoOrden.CREADA);
    }

    @Test
    void cadaCambioDeEstadoQuedaRegistradoEnElHistorial() {
        Orden orden = Fixtures.orden(1L, "LG-AAAA1111");

        orden.asignar(chofer);
        orden.iniciarTransito(chofer);
        orden.marcarEntregada(chofer);

        assertThat(orden.getEstado()).isEqualTo(EstadoOrden.ENTREGADA);
        assertThat(orden.getEventos()).extracting(EventoOrden::getEstadoNuevo)
                .containsExactly(EstadoOrden.CREADA, EstadoOrden.ASIGNADA, EstadoOrden.EN_TRANSITO, EstadoOrden.ENTREGADA);
    }

    @Test
    void noSePuedeCancelarUnaOrdenEnTransito() {
        Orden orden = Fixtures.orden(1L, "LG-AAAA1111");
        orden.asignar(chofer);
        orden.iniciarTransito(chofer);

        assertThatThrownBy(() -> orden.cancelar(chofer))
                .isInstanceOf(TransicionInvalidaException.class)
                .hasMessageContaining("EN_TRANSITO");
        assertThat(orden.getEstado()).isEqualTo(EstadoOrden.EN_TRANSITO);
    }

    @Test
    void unaEntregaFallidaSumaUnIntentoYPermiteReplanificar() {
        Orden orden = Fixtures.orden(1L, "LG-AAAA1111");
        orden.asignar(chofer);
        orden.iniciarTransito(chofer);

        orden.marcarNoEntregada("Cliente ausente", chofer);

        assertThat(orden.getIntentosFallidos()).isEqualTo(1);
        assertThat(orden.puedePlanificarse()).isTrue();
    }

    @Test
    void conDosIntentosFallidosYaNoSePuedeAsignar() {
        Orden orden = Fixtures.orden(1L, "LG-AAAA1111");
        for (int i = 0; i < Orden.MAX_INTENTOS; i++) {
            orden.asignar(chofer);
            orden.iniciarTransito(chofer);
            orden.marcarNoEntregada("Cliente ausente", chofer);
        }

        assertThat(orden.agotoIntentos()).isTrue();
        assertThat(orden.puedePlanificarse()).isFalse();
        assertThatThrownBy(() -> orden.asignar(chofer)).isInstanceOf(TransicionInvalidaException.class);
    }
}
