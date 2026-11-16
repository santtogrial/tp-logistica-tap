package ar.edu.up.tap.logistica.tracking;

import java.time.Instant;

/** Posición en vivo de un vehículo. No se persiste: solo se guarda la última en memoria. */
public record PosicionVehiculo(Long vehiculoId, String patente, double lat, double lng, Instant instante) {
}
