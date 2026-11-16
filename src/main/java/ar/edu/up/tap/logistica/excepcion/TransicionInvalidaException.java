package ar.edu.up.tap.logistica.excepcion;

public class TransicionInvalidaException extends ReglaNegocioException {

    public TransicionInvalidaException(String mensaje) {
        super(mensaje);
    }
}
