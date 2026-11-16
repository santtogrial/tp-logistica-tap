package ar.edu.up.tap.logistica;

import ar.edu.up.tap.logistica.dominio.*;
import org.springframework.test.util.ReflectionTestUtils;

/** Objetos de dominio listos para usar en los tests. */
public final class Fixtures {

    private Fixtures() {
    }

    public static <T> T conId(T entidad, Long id) {
        ReflectionTestUtils.setField(entidad, "id", id);
        return entidad;
    }

    public static Usuario operador() {
        return conId(new Usuario("Laura Gómez", "operador@logistica.com", "hash", Rol.OPERADOR), 2L);
    }

    public static Usuario chofer(Long id) {
        return conId(new Usuario("Chofer " + id, "chofer" + id + "@logistica.com", "hash", Rol.CHOFER), id);
    }

    public static Deposito deposito() {
        return conId(new Deposito("Depósito Palermo", "Av. Juan B. Justo 1500", new Coordenada(-34.5885, -58.4320)), 1L);
    }

    public static Destinatario destinatario(Long id, double lat, double lng) {
        return conId(new Destinatario("Cliente " + id, "11 5555-0000", "Calle " + id, new Coordenada(lat, lng)), id);
    }

    public static Producto producto(Long id) {
        return conId(new Producto("SKU-" + id, "Producto " + id), id);
    }

    public static Orden orden(Long id, String codigo) {
        Orden orden = new Orden(codigo, deposito(), destinatario(id, -34.60, -58.42), operador());
        orden.agregarItem(producto(10L), 2);
        return conId(orden, id);
    }

    public static Vehiculo vehiculo(Long id, Usuario chofer) {
        return conId(new Vehiculo("AB" + id + "23CD", "Fiat Fiorino", deposito(), chofer), id);
    }
}
