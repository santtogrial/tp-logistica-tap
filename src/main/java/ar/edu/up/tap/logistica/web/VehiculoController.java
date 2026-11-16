package ar.edu.up.tap.logistica.web;

import ar.edu.up.tap.logistica.dominio.EstadoVehiculo;
import ar.edu.up.tap.logistica.servicio.TrackingService;
import ar.edu.up.tap.logistica.servicio.VehiculoService;
import ar.edu.up.tap.logistica.tracking.PosicionVehiculo;
import ar.edu.up.tap.logistica.web.dto.CambiarEstadoVehiculoRequest;
import ar.edu.up.tap.logistica.web.dto.PaginaResponse;
import ar.edu.up.tap.logistica.web.dto.PosicionRequest;
import ar.edu.up.tap.logistica.web.dto.VehiculoResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/vehiculos")
public class VehiculoController {

    private final VehiculoService vehiculoService;
    private final TrackingService trackingService;

    public VehiculoController(VehiculoService vehiculoService, TrackingService trackingService) {
        this.vehiculoService = vehiculoService;
        this.trackingService = trackingService;
    }

    @GetMapping
    public PaginaResponse<VehiculoResponse> listar(@RequestParam(required = false) EstadoVehiculo estado,
                                                   @PageableDefault(sort = "id") Pageable pageable) {
        return vehiculoService.listar(estado, pageable);
    }

    @PatchMapping("/{id}/estado")
    public VehiculoResponse cambiarEstado(@PathVariable Long id, @Valid @RequestBody CambiarEstadoVehiculoRequest request) {
        return vehiculoService.cambiarEstado(id, request.estado());
    }

    @PostMapping("/{id}/posicion")
    public PosicionVehiculo informarPosicion(@PathVariable Long id, @Valid @RequestBody PosicionRequest request) {
        return trackingService.registrarPosicion(id, request);
    }
}
