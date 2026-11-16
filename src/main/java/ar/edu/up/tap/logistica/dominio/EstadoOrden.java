package ar.edu.up.tap.logistica.dominio;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * Estados de una orden y las transiciones permitidas entre ellos.
 * Toda la regla de "qué puede pasar a qué" vive acá; Orden solo la consulta.
 */
public enum EstadoOrden {
    CREADA,
    ASIGNADA,
    EN_TRANSITO,
    ENTREGADA,
    NO_ENTREGADA,
    DEVUELTA,
    CANCELADA;

    private static final Map<EstadoOrden, Set<EstadoOrden>> TRANSICIONES = new EnumMap<>(EstadoOrden.class);

    static {
        TRANSICIONES.put(CREADA, EnumSet.of(ASIGNADA, CANCELADA));
        TRANSICIONES.put(ASIGNADA, EnumSet.of(EN_TRANSITO, CREADA));
        TRANSICIONES.put(EN_TRANSITO, EnumSet.of(ENTREGADA, NO_ENTREGADA));
        TRANSICIONES.put(NO_ENTREGADA, EnumSet.of(ASIGNADA, DEVUELTA));
        TRANSICIONES.put(ENTREGADA, EnumSet.noneOf(EstadoOrden.class));
        TRANSICIONES.put(DEVUELTA, EnumSet.noneOf(EstadoOrden.class));
        TRANSICIONES.put(CANCELADA, EnumSet.noneOf(EstadoOrden.class));
    }

    public boolean puedePasarA(EstadoOrden destino) {
        return TRANSICIONES.get(this).contains(destino);
    }

    public boolean esFinal() {
        return TRANSICIONES.get(this).isEmpty();
    }
}
