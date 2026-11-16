package ar.edu.up.tap.logistica.excepcion;

public class StockInsuficienteException extends ReglaNegocioException {

    public StockInsuficienteException(String producto, int disponible, int pedido) {
        super("Stock insuficiente de " + producto + ": hay " + disponible + " y se pidieron " + pedido);
    }
}
