package ar.edu.up.tap.logistica.web;

import ar.edu.up.tap.logistica.servicio.SeguimientoService;
import ar.edu.up.tap.logistica.web.dto.SeguimientoResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Endpoint público: el destinatario consulta su pedido con el código, sin iniciar sesión. */
@RestController
@RequestMapping("/api/seguimiento")
public class SeguimientoController {

    private final SeguimientoService seguimientoService;

    public SeguimientoController(SeguimientoService seguimientoService) {
        this.seguimientoService = seguimientoService;
    }

    @GetMapping("/{codigo}")
    public SeguimientoResponse consultar(@PathVariable String codigo) {
        return seguimientoService.consultar(codigo);
    }
}
