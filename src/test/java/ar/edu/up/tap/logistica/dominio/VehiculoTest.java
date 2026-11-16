package ar.edu.up.tap.logistica.dominio;

import ar.edu.up.tap.logistica.Fixtures;
import ar.edu.up.tap.logistica.excepcion.TransicionInvalidaException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VehiculoTest {

    @Test
    void unVehiculoEnTallerNoPuedeSalirARuta() {
        Vehiculo vehiculo = Fixtures.vehiculo(1L, Fixtures.chofer(3L));
        vehiculo.cambiarEstado(EstadoVehiculo.EN_TALLER);

        assertThatThrownBy(vehiculo::salirARuta).isInstanceOf(TransicionInvalidaException.class);
    }

    @Test
    void unVehiculoEnRutaNoSePuedeMandarAlTallerAMano() {
        Vehiculo vehiculo = Fixtures.vehiculo(1L, Fixtures.chofer(3L));
        vehiculo.salirARuta();

        assertThatThrownBy(() -> vehiculo.cambiarEstado(EstadoVehiculo.EN_TALLER))
                .isInstanceOf(TransicionInvalidaException.class);
        assertThat(vehiculo.getEstado()).isEqualTo(EstadoVehiculo.EN_RUTA);
    }

    @Test
    void reconoceASuChofer() {
        Usuario chofer = Fixtures.chofer(3L);
        Vehiculo vehiculo = Fixtures.vehiculo(1L, chofer);

        assertThat(vehiculo.esConducidoPor(chofer)).isTrue();
        assertThat(vehiculo.esConducidoPor(Fixtures.chofer(4L))).isFalse();
    }
}
