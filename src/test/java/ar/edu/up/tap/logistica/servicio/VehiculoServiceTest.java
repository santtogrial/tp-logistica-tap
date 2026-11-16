package ar.edu.up.tap.logistica.servicio;

import ar.edu.up.tap.logistica.Fixtures;
import ar.edu.up.tap.logistica.dominio.EstadoVehiculo;
import ar.edu.up.tap.logistica.dominio.Vehiculo;
import ar.edu.up.tap.logistica.excepcion.RecursoNoEncontradoException;
import ar.edu.up.tap.logistica.repositorio.VehiculoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VehiculoServiceTest {

    @Mock
    private VehiculoRepository vehiculoRepository;

    @InjectMocks
    private VehiculoService vehiculoService;

    @Test
    void elAdminPuedeMandarUnVehiculoDisponibleAlTaller() {
        Vehiculo vehiculo = Fixtures.vehiculo(1L, Fixtures.chofer(3L));
        when(vehiculoRepository.findById(1L)).thenReturn(Optional.of(vehiculo));

        assertThat(vehiculoService.cambiarEstado(1L, EstadoVehiculo.EN_TALLER).estado()).isEqualTo(EstadoVehiculo.EN_TALLER);
    }

    @Test
    void unVehiculoInexistenteResponde404() {
        when(vehiculoRepository.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> vehiculoService.cambiarEstado(9L, EstadoVehiculo.EN_TALLER))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }
}
