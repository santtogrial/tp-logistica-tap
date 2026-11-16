package ar.edu.up.tap.logistica.web.dto;

import ar.edu.up.tap.logistica.dominio.ResultadoEntrega;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ResultadoParadaRequest(@NotNull ResultadoEntrega resultado, @Size(max = 255) String motivo) {
}
