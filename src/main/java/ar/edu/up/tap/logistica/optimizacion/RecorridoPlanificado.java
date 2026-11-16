package ar.edu.up.tap.logistica.optimizacion;

import java.util.List;

public record RecorridoPlanificado(Long vehiculoId, List<ParadaPlanificada> paradas, double distanciaKm, int duracionMin) {
}
