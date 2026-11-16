package ar.edu.up.tap.logistica.web.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * Si no se mandan ordenIds se planifican todas las órdenes pendientes del depósito.
 */
public record PlanificacionRequest(@NotNull Long depositoId,
                                   @NotEmpty List<@NotNull Long> vehiculoIds,
                                   List<@NotNull Long> ordenIds) {
}
