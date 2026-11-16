package ar.edu.up.tap.logistica.excepcion;

/**
 * Operación válida en forma pero que viola una regla del negocio.
 * Se responde con 409 Conflict.
 */
public class ReglaNegocioException extends RuntimeException {

    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }
}
