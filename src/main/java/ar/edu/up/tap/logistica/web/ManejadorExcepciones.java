package ar.edu.up.tap.logistica.web;

import ar.edu.up.tap.logistica.excepcion.RecursoNoEncontradoException;
import ar.edu.up.tap.logistica.excepcion.ReglaNegocioException;
import ar.edu.up.tap.logistica.web.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Traduce las excepciones a respuestas HTTP con un formato único, para que ningún error
 * llegue al cliente como un stack trace.
 */
@RestControllerAdvice
public class ManejadorExcepciones {

    private static final Logger log = LoggerFactory.getLogger(ManejadorExcepciones.class);

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ErrorResponse> noEncontrado(RecursoNoEncontradoException e, HttpServletRequest req) {
        return responder(HttpStatus.NOT_FOUND, e.getMessage(), req);
    }

    @ExceptionHandler(ReglaNegocioException.class)
    public ResponseEntity<ErrorResponse> reglaNegocio(ReglaNegocioException e, HttpServletRequest req) {
        log.info("Regla de negocio en {}: {}", req.getRequestURI(), e.getMessage());
        return responder(HttpStatus.CONFLICT, e.getMessage(), req);
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ErrorResponse> concurrencia(ObjectOptimisticLockingFailureException e, HttpServletRequest req) {
        return responder(HttpStatus.CONFLICT, "Otro usuario modificó los mismos datos al mismo tiempo. Reintentá la operación", req);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> validacion(MethodArgumentNotValidException e, HttpServletRequest req) {
        List<String> detalles = e.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + ": " + f.getDefaultMessage())
                .toList();
        ErrorResponse cuerpo = new ErrorResponse(LocalDateTime.now(), 400, "Bad Request",
                "Hay datos inválidos en el pedido", req.getRequestURI(), detalles);
        return ResponseEntity.badRequest().body(cuerpo);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ErrorResponse> formatoInvalido(Exception e, HttpServletRequest req) {
        return responder(HttpStatus.BAD_REQUEST, "El formato del pedido no es válido (revisá tipos y valores permitidos)", req);
    }

    @ExceptionHandler({BadCredentialsException.class, DisabledException.class})
    public ResponseEntity<ErrorResponse> credenciales(AuthenticationException e, HttpServletRequest req) {
        log.info("Login rechazado en {}", req.getRequestURI());
        return responder(HttpStatus.UNAUTHORIZED, "Email o contraseña incorrectos", req);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> noAutenticado(AuthenticationException e, HttpServletRequest req) {
        return responder(HttpStatus.UNAUTHORIZED, e.getMessage(), req);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> accesoDenegado(AccessDeniedException e, HttpServletRequest req) {
        return responder(HttpStatus.FORBIDDEN, e.getMessage(), req);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> rutaInexistente(NoResourceFoundException e, HttpServletRequest req) {
        return responder(HttpStatus.NOT_FOUND, "El endpoint no existe", req);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> metodoNoPermitido(HttpRequestMethodNotSupportedException e, HttpServletRequest req) {
        return responder(HttpStatus.METHOD_NOT_ALLOWED, "Método " + e.getMethod() + " no permitido en este endpoint", req);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> inesperado(Exception e, HttpServletRequest req) {
        log.error("Error no controlado en {} {}", req.getMethod(), req.getRequestURI(), e);
        return responder(HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrió un error inesperado", req);
    }

    private ResponseEntity<ErrorResponse> responder(HttpStatus status, String mensaje, HttpServletRequest req) {
        return ResponseEntity.status(status)
                .body(ErrorResponse.de(status.value(), status.getReasonPhrase(), mensaje, req.getRequestURI()));
    }
}
