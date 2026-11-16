package ar.edu.up.tap.logistica.web.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CrearOrdenRequest(@NotNull Long depositoId,
                                @NotNull Long destinatarioId,
                                @NotEmpty List<@Valid ItemRequest> items) {
}
