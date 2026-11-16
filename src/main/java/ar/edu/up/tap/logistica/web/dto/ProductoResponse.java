package ar.edu.up.tap.logistica.web.dto;

import ar.edu.up.tap.logistica.dominio.Producto;

public record ProductoResponse(Long id, String sku, String nombre) {

    public static ProductoResponse desde(Producto producto) {
        return new ProductoResponse(producto.getId(), producto.getSku(), producto.getNombre());
    }
}
