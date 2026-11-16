package ar.edu.up.tap.logistica.web.dto;

import ar.edu.up.tap.logistica.dominio.EstadoVehiculo;
import jakarta.validation.constraints.NotNull;

public record CambiarEstadoVehiculoRequest(@NotNull EstadoVehiculo estado) {
}
