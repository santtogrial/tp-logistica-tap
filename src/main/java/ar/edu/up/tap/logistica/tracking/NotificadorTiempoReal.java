package ar.edu.up.tap.logistica.tracking;

public interface NotificadorTiempoReal {

    /** Para el panel del operador: todas las posiciones de vehículos en ruta. */
    void enviarPosicion(PosicionVehiculo posicion);

    /** Para el destinatario que sigue su pedido con el código de seguimiento. */
    void enviarSeguimiento(MensajeSeguimiento mensaje);
}
