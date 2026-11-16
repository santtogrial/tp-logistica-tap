package ar.edu.up.tap.logistica.web;

import ar.edu.up.tap.logistica.dominio.EstadoRuta;
import ar.edu.up.tap.logistica.servicio.RutaService;
import ar.edu.up.tap.logistica.web.dto.PaginaResponse;
import ar.edu.up.tap.logistica.web.dto.ResultadoParadaRequest;
import ar.edu.up.tap.logistica.web.dto.RutaResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class RutaController {

    private final RutaService rutaService;

    public RutaController(RutaService rutaService) {
        this.rutaService = rutaService;
    }

    @GetMapping("/rutas")
    public PaginaResponse<RutaResponse> listar(@RequestParam(required = false) EstadoRuta estado,
                                               @PageableDefault(sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        return rutaService.listar(estado, pageable);
    }

    @GetMapping("/rutas/actual")
    public RutaResponse rutaActual() {
        return rutaService.rutaActual();
    }

    @GetMapping("/rutas/{id}")
    public RutaResponse obtener(@PathVariable Long id) {
        return rutaService.obtener(id);
    }

    @PostMapping("/rutas/{id}/iniciar")
    public RutaResponse iniciar(@PathVariable Long id) {
        return rutaService.iniciar(id);
    }

    @PostMapping("/rutas/{id}/finalizar")
    public RutaResponse finalizar(@PathVariable Long id) {
        return rutaService.finalizar(id);
    }

    @PostMapping("/rutas/{id}/anular")
    public RutaResponse anular(@PathVariable Long id) {
        return rutaService.anular(id);
    }

    @PostMapping("/paradas/{id}/resultado")
    public RutaResponse registrarResultado(@PathVariable Long id, @Valid @RequestBody ResultadoParadaRequest request) {
        return rutaService.registrarResultado(id, request);
    }
}
