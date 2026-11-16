package ar.edu.up.tap.logistica.optimizacion;

import ar.edu.up.tap.logistica.dominio.Coordenada;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Heurística en dos pasos:
 * 1. Barrido: ordena los puntos por ángulo respecto del depósito y los reparte en bloques
 *    consecutivos entre los vehículos, así cada uno atiende una "porción" de la ciudad.
 * 2. Vecino más cercano: dentro de cada bloque, desde el depósito va siempre a la parada más cercana.
 * No garantiza el óptimo, pero es O(n²), predecible y suficiente para el volumen del TP.
 */
@Component
public class OptimizadorVecinoMasCercano implements OptimizadorRutas {

    private final double velocidadKmh;
    private final int minutosPorParada;

    public OptimizadorVecinoMasCercano(@Value("${logistica.optimizador.velocidad-kmh}") double velocidadKmh,
                                       @Value("${logistica.optimizador.minutos-por-parada}") int minutosPorParada) {
        this.velocidadKmh = velocidadKmh;
        this.minutosPorParada = minutosPorParada;
    }

    @Override
    public List<RecorridoPlanificado> optimizar(Coordenada origen, List<Long> vehiculoIds, List<PuntoEntrega> puntos) {
        if (vehiculoIds.isEmpty()) {
            throw new IllegalArgumentException("Se necesita al menos un vehículo");
        }
        List<List<PuntoEntrega>> bloques = repartirPorBarrido(origen, vehiculoIds.size(), puntos);

        List<RecorridoPlanificado> recorridos = new ArrayList<>();
        for (int i = 0; i < vehiculoIds.size(); i++) {
            List<PuntoEntrega> bloque = bloques.get(i);
            if (!bloque.isEmpty()) {
                recorridos.add(armarRecorrido(origen, vehiculoIds.get(i), bloque));
            }
        }
        return recorridos;
    }

    private List<List<PuntoEntrega>> repartirPorBarrido(Coordenada origen, int cantidadVehiculos, List<PuntoEntrega> puntos) {
        List<PuntoEntrega> ordenados = new ArrayList<>(puntos);
        ordenados.sort(Comparator.comparingDouble(p -> angulo(origen, p.ubicacion())));

        List<List<PuntoEntrega>> bloques = new ArrayList<>();
        int base = ordenados.size() / cantidadVehiculos;
        int resto = ordenados.size() % cantidadVehiculos;
        int desde = 0;
        for (int i = 0; i < cantidadVehiculos; i++) {
            int tamanio = base + (i < resto ? 1 : 0);
            bloques.add(new ArrayList<>(ordenados.subList(desde, desde + tamanio)));
            desde += tamanio;
        }
        return bloques;
    }

    private RecorridoPlanificado armarRecorrido(Coordenada origen, Long vehiculoId, List<PuntoEntrega> pendientes) {
        List<ParadaPlanificada> paradas = new ArrayList<>();
        Coordenada actual = origen;
        double distanciaAcumulada = 0;
        int secuencia = 1;

        while (!pendientes.isEmpty()) {
            PuntoEntrega masCercano = buscarMasCercano(actual, pendientes);
            distanciaAcumulada += actual.distanciaKm(masCercano.ubicacion());
            int eta = minutosDeViaje(distanciaAcumulada) + (secuencia - 1) * minutosPorParada;
            paradas.add(new ParadaPlanificada(masCercano.ordenId(), secuencia, eta));

            actual = masCercano.ubicacion();
            pendientes.remove(masCercano);
            secuencia++;
        }

        // El vehículo vuelve al depósito al terminar
        double distanciaTotal = distanciaAcumulada + actual.distanciaKm(origen);
        int duracion = minutosDeViaje(distanciaTotal) + paradas.size() * minutosPorParada;
        return new RecorridoPlanificado(vehiculoId, paradas, redondear(distanciaTotal), duracion);
    }

    private PuntoEntrega buscarMasCercano(Coordenada desde, List<PuntoEntrega> candidatos) {
        return candidatos.stream()
                .min(Comparator.comparingDouble(p -> desde.distanciaKm(p.ubicacion())))
                .orElseThrow();
    }

    private double angulo(Coordenada origen, Coordenada punto) {
        return Math.atan2(punto.getLat() - origen.getLat(), punto.getLng() - origen.getLng());
    }

    private int minutosDeViaje(double km) {
        return (int) Math.round(km / velocidadKmh * 60);
    }

    private double redondear(double km) {
        return Math.round(km * 100) / 100.0;
    }
}
