package ar.edu.up.tap.logistica.dominio;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class EstadoOrdenTest {

    @ParameterizedTest(name = "{0} -> {1} es válida")
    @CsvSource({
            "CREADA, ASIGNADA",
            "CREADA, CANCELADA",
            "ASIGNADA, EN_TRANSITO",
            "ASIGNADA, CREADA",
            "EN_TRANSITO, ENTREGADA",
            "EN_TRANSITO, NO_ENTREGADA",
            "NO_ENTREGADA, ASIGNADA",
            "NO_ENTREGADA, DEVUELTA"
    })
    void transicionesValidas(EstadoOrden desde, EstadoOrden hacia) {
        assertThat(desde.puedePasarA(hacia)).isTrue();
    }

    @ParameterizedTest(name = "{0} -> {1} no es válida")
    @CsvSource({
            "CREADA, EN_TRANSITO",
            "CREADA, ENTREGADA",
            "EN_TRANSITO, CANCELADA",
            "EN_TRANSITO, CREADA",
            "ENTREGADA, NO_ENTREGADA",
            "DEVUELTA, ASIGNADA",
            "CANCELADA, CREADA"
    })
    void transicionesInvalidas(EstadoOrden desde, EstadoOrden hacia) {
        assertThat(desde.puedePasarA(hacia)).isFalse();
    }

    @Test
    void soloEntregadaDevueltaYCanceladaSonFinales() {
        assertThat(EstadoOrden.ENTREGADA.esFinal()).isTrue();
        assertThat(EstadoOrden.DEVUELTA.esFinal()).isTrue();
        assertThat(EstadoOrden.CANCELADA.esFinal()).isTrue();
        assertThat(EstadoOrden.CREADA.esFinal()).isFalse();
        assertThat(EstadoOrden.NO_ENTREGADA.esFinal()).isFalse();
    }
}
