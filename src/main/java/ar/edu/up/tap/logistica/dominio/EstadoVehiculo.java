package ar.edu.up.tap.logistica.dominio;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public enum EstadoVehiculo {
    DISPONIBLE,
    EN_RUTA,
    EN_TALLER,
    BAJA;

    // Cambios que puede hacer un administrador a mano. EN_RUTA solo lo maneja el ciclo de la ruta.
    private static final Map<EstadoVehiculo, Set<EstadoVehiculo>> CAMBIOS_MANUALES = new EnumMap<>(EstadoVehiculo.class);

    static {
        CAMBIOS_MANUALES.put(DISPONIBLE, EnumSet.of(EN_TALLER, BAJA));
        CAMBIOS_MANUALES.put(EN_TALLER, EnumSet.of(DISPONIBLE, BAJA));
        CAMBIOS_MANUALES.put(EN_RUTA, EnumSet.noneOf(EstadoVehiculo.class));
        CAMBIOS_MANUALES.put(BAJA, EnumSet.noneOf(EstadoVehiculo.class));
    }

    public boolean admiteCambioManualA(EstadoVehiculo destino) {
        return CAMBIOS_MANUALES.get(this).contains(destino);
    }
}
