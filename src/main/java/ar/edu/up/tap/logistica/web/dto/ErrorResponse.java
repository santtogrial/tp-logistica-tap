package ar.edu.up.tap.logistica.web.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ErrorResponse(LocalDateTime fecha, int status, String error, String mensaje, String ruta, List<String> detalles) {

    public static ErrorResponse de(int status, String error, String mensaje, String ruta) {
        return new ErrorResponse(LocalDateTime.now(), status, error, mensaje, ruta, List.of());
    }
}
