package ar.edu.up.tap.logistica.web.dto;

import ar.edu.up.tap.logistica.dominio.EstadoOrden;
import ar.edu.up.tap.logistica.dominio.EventoOrden;
import ar.edu.up.tap.logistica.dominio.ItemOrden;
import ar.edu.up.tap.logistica.dominio.Orden;

import java.time.LocalDateTime;
import java.util.List;

public record OrdenDetalleResponse(Long id, String codigoSeguimiento, EstadoOrden estado, DepositoResponse deposito,
                                   DestinatarioResponse destinatario, int intentosFallidos, LocalDateTime creadaEn,
                                   List<Item> items, List<Evento> historial) {

    public record Item(Long productoId, String sku, String producto, int cantidad) {
        static Item desde(ItemOrden item) {
            return new Item(item.getProducto().getId(), item.getProducto().getSku(),
                    item.getProducto().getNombre(), item.getCantidad());
        }
    }

    public record Evento(EstadoOrden estadoAnterior, EstadoOrden estadoNuevo, LocalDateTime fecha,
                         String usuario, String observacion) {
        static Evento desde(EventoOrden evento) {
            String usuario = evento.getUsuario() != null ? evento.getUsuario().getNombre() : null;
            return new Evento(evento.getEstadoAnterior(), evento.getEstadoNuevo(), evento.getFecha(),
                    usuario, evento.getObservacion());
        }
    }

    public static OrdenDetalleResponse desde(Orden orden) {
        return new OrdenDetalleResponse(orden.getId(), orden.getCodigoSeguimiento(), orden.getEstado(),
                DepositoResponse.desde(orden.getDeposito()), DestinatarioResponse.desde(orden.getDestinatario()),
                orden.getIntentosFallidos(), orden.getCreadaEn(),
                orden.getItems().stream().map(Item::desde).toList(),
                orden.getEventos().stream().map(Evento::desde).toList());
    }
}
