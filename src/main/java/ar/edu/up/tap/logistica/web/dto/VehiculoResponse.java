package ar.edu.up.tap.logistica.web.dto;

import ar.edu.up.tap.logistica.dominio.EstadoVehiculo;
import ar.edu.up.tap.logistica.dominio.Vehiculo;

public record VehiculoResponse(Long id, String patente, String modelo, EstadoVehiculo estado,
                               Long depositoBaseId, String chofer) {

    public static VehiculoResponse desde(Vehiculo v) {
        String chofer = v.getChofer() != null ? v.getChofer().getNombre() : null;
        return new VehiculoResponse(v.getId(), v.getPatente(), v.getModelo(), v.getEstado(),
                v.getDepositoBase().getId(), chofer);
    }
}
