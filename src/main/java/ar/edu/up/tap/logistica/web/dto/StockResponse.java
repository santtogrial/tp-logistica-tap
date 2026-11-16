package ar.edu.up.tap.logistica.web.dto;

import ar.edu.up.tap.logistica.dominio.StockDeposito;

public record StockResponse(Long productoId, String sku, String producto, int cantidad) {

    public static StockResponse desde(StockDeposito stock) {
        return new StockResponse(stock.getProducto().getId(), stock.getProducto().getSku(),
                stock.getProducto().getNombre(), stock.getCantidad());
    }
}
