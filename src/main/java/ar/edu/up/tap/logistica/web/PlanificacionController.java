package ar.edu.up.tap.logistica.web;

import ar.edu.up.tap.logistica.servicio.PlanificacionService;
import ar.edu.up.tap.logistica.web.dto.PlanificacionRequest;
import ar.edu.up.tap.logistica.web.dto.PlanificacionResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/planificaciones")
public class PlanificacionController {

    private final PlanificacionService planificacionService;

    public PlanificacionController(PlanificacionService planificacionService) {
        this.planificacionService = planificacionService;
    }

    @PostMapping
    public ResponseEntity<PlanificacionResponse> planificar(@Valid @RequestBody PlanificacionRequest request) {
        PlanificacionResponse planificacion = planificacionService.planificar(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .location(URI.create("/api/planificaciones/" + planificacion.id()))
                .body(planificacion);
    }

    @GetMapping("/{id}")
    public PlanificacionResponse obtener(@PathVariable Long id) {
        return planificacionService.obtener(id);
    }
}
