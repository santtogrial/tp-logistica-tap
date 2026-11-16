package ar.edu.up.tap.logistica.web.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CrearDestinatarioRequest(@NotBlank @Size(max = 100) String nombre,
                                       @Size(max = 30) String telefono,
                                       @NotBlank @Size(max = 200) String direccion,
                                       @NotNull @DecimalMin("-90") @DecimalMax("90") Double lat,
                                       @NotNull @DecimalMin("-180") @DecimalMax("180") Double lng) {
}
