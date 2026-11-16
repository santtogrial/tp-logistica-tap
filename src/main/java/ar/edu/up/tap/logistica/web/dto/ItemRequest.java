package ar.edu.up.tap.logistica.web.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ItemRequest(@NotNull Long productoId, @NotNull @Positive Integer cantidad) {
}
