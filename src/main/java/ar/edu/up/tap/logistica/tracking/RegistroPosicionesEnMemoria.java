package ar.edu.up.tap.logistica.tracking;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Guarda solo la última posición de cada vehículo. Si la API se reinicia se pierde,
 * lo cual es aceptable porque el chofer manda una nueva en segundos.
 * Para escalar a varias instancias se reemplaza por una implementación sobre Redis.
 */
@Component
public class RegistroPosicionesEnMemoria implements RegistroPosiciones {

    private final Map<Long, PosicionVehiculo> posiciones = new ConcurrentHashMap<>();

    @Override
    public void guardar(PosicionVehiculo posicion) {
        posiciones.put(posicion.vehiculoId(), posicion);
    }

    @Override
    public Optional<PosicionVehiculo> ultima(Long vehiculoId) {
        return Optional.ofNullable(posiciones.get(vehiculoId));
    }

    @Override
    public void eliminar(Long vehiculoId) {
        posiciones.remove(vehiculoId);
    }
}
