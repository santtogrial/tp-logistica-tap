package ar.edu.up.tap.logistica.web.dto;

import ar.edu.up.tap.logistica.dominio.Deposito;

public record DepositoResponse(Long id, String nombre, String direccion, double lat, double lng) {

    public static DepositoResponse desde(Deposito deposito) {
        return new DepositoResponse(deposito.getId(), deposito.getNombre(), deposito.getDireccion(),
                deposito.getUbicacion().getLat(), deposito.getUbicacion().getLng());
    }
}
