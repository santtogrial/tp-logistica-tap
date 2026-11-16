package ar.edu.up.tap.logistica.web.dto;

import ar.edu.up.tap.logistica.dominio.Destinatario;

public record DestinatarioResponse(Long id, String nombre, String telefono, String direccion, double lat, double lng) {

    public static DestinatarioResponse desde(Destinatario d) {
        return new DestinatarioResponse(d.getId(), d.getNombre(), d.getTelefono(), d.getDireccion(),
                d.getUbicacion().getLat(), d.getUbicacion().getLng());
    }
}
