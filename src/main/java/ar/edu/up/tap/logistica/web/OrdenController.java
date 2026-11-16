package ar.edu.up.tap.logistica.web;

import ar.edu.up.tap.logistica.dominio.EstadoOrden;
import ar.edu.up.tap.logistica.servicio.OrdenService;
import ar.edu.up.tap.logistica.web.dto.CrearOrdenRequest;
import ar.edu.up.tap.logistica.web.dto.OrdenDetalleResponse;
import ar.edu.up.tap.logistica.web.dto.OrdenResumenResponse;
import ar.edu.up.tap.logistica.web.dto.PaginaResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/ordenes")
public class OrdenController {

    private final OrdenService ordenService;

    public OrdenController(OrdenService ordenService) {
        this.ordenService = ordenService;
    }

    @GetMapping
    public PaginaResponse<OrdenResumenResponse> listar(
            @RequestParam(required = false) EstadoOrden estado,
            @RequestParam(required = false) Long depositoId,
            @PageableDefault(sort = "creadaEn", direction = Sort.Direction.DESC) Pageable pageable) {
        return ordenService.listar(estado, depositoId, pageable);
    }

    @GetMapping("/{id}")
    public OrdenDetalleResponse obtener(@PathVariable Long id) {
        return ordenService.obtener(id);
    }

    @PostMapping
    public ResponseEntity<OrdenDetalleResponse> crear(@Valid @RequestBody CrearOrdenRequest request) {
        OrdenDetalleResponse orden = ordenService.crear(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .location(URI.create("/api/ordenes/" + orden.id()))
                .body(orden);
    }

    @PostMapping("/{id}/cancelar")
    public OrdenDetalleResponse cancelar(@PathVariable Long id) {
        return ordenService.cancelar(id);
    }
}
