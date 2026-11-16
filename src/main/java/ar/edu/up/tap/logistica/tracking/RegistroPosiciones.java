package ar.edu.up.tap.logistica.tracking;

import java.util.Optional;

public interface RegistroPosiciones {

    void guardar(PosicionVehiculo posicion);

    Optional<PosicionVehiculo> ultima(Long vehiculoId);

    void eliminar(Long vehiculoId);
}
