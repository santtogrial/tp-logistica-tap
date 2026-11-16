package ar.edu.up.tap.logistica.optimizacion;

import ar.edu.up.tap.logistica.dominio.Coordenada;

import java.util.List;

/**
 * Calcula los recorridos de entrega. La planificación depende de esta interfaz y no de un
 * algoritmo concreto: hoy se usa una heurística en Java y el plan de mejora es reemplazarla
 * por un microservicio en Python con OR-Tools sin tocar PlanificacionService.
 */
public interface OptimizadorRutas {

    List<RecorridoPlanificado> optimizar(Coordenada origen, List<Long> vehiculoIds, List<PuntoEntrega> puntos);
}
