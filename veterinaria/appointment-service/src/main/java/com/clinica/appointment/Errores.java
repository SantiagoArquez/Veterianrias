package com.clinica.appointment;

import com.clinica.appointment.Dtos.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

/**
 * Todo el manejo de errores en un solo archivo:
 *  - Las excepciones propias del negocio (cada una se traduce a un código HTTP).
 *  - El "Manejador", que atrapa esas excepciones y arma la respuesta JSON de error.
 */
public class Errores {

    private Errores() {
    }

    /** El recurso no existe -> HTTP 404. */
    public static class NoEncontrado extends RuntimeException {
        public NoEncontrado(String mensaje) { super(mensaje); }
    }

    /** Se incumple una regla de negocio o un dato es inválido -> HTTP 400. */
    public static class ReglaNegocio extends RuntimeException {
        public ReglaNegocio(String mensaje) { super(mensaje); }
    }

    /** Choque con el estado actual (duplicados, borrar con dependencias...) -> HTTP 409. */
    public static class Conflicto extends RuntimeException {
        public Conflicto(String mensaje) { super(mensaje); }
    }

    /** Otro microservicio no responde -> HTTP 503. */
    public static class ServicioNoDisponible extends RuntimeException {
        public ServicioNoDisponible(String mensaje) { super(mensaje); }
    }

    /** Convierte cada excepción en una respuesta HTTP con el cuerpo ApiError. */
    @RestControllerAdvice
    public static class Manejador {

        private static final Logger log = LoggerFactory.getLogger(Manejador.class);

        @ExceptionHandler(NoEncontrado.class)
        public ResponseEntity<ApiError> noEncontrado(NoEncontrado ex, HttpServletRequest req) {
            return responder(HttpStatus.NOT_FOUND, ex.getMessage(), req);
        }

        @ExceptionHandler(ReglaNegocio.class)
        public ResponseEntity<ApiError> reglaNegocio(ReglaNegocio ex, HttpServletRequest req) {
            return responder(HttpStatus.BAD_REQUEST, ex.getMessage(), req);
        }

        @ExceptionHandler(Conflicto.class)
        public ResponseEntity<ApiError> conflicto(Conflicto ex, HttpServletRequest req) {
            return responder(HttpStatus.CONFLICT, ex.getMessage(), req);
        }

        /** Última defensa si la base de datos rechaza algo (por ejemplo, un duplicado concurrente). */
        @ExceptionHandler(DataIntegrityViolationException.class)
        public ResponseEntity<ApiError> integridad(DataIntegrityViolationException ex, HttpServletRequest req) {
            return responder(HttpStatus.CONFLICT, "La operación viola una restricción de integridad de datos", req);
        }

        @ExceptionHandler(ServicioNoDisponible.class)
        public ResponseEntity<ApiError> noDisponible(ServicioNoDisponible ex, HttpServletRequest req) {
            return responder(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage(), req);
        }

        /** Falla alguna validación del DTO (@NotBlank, @Email...): se listan los campos con error. */
        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ApiError> validacion(MethodArgumentNotValidException ex, HttpServletRequest req) {
            String detalle = ex.getBindingResult().getFieldErrors().stream()
                    .map(e -> e.getField() + ": " + e.getDefaultMessage())
                    .sorted()
                    .collect(Collectors.joining("; "));
            return responder(HttpStatus.BAD_REQUEST, detalle, req);
        }

        /** JSON mal formado o parámetro con tipo incorrecto. */
        @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
        public ResponseEntity<ApiError> peticionInvalida(Exception ex, HttpServletRequest req) {
            return responder(HttpStatus.BAD_REQUEST, "La petición tiene un formato inválido", req);
        }

        /** Cualquier error no previsto: se registra en el log y se responde 500 sin detalles internos. */
        @ExceptionHandler(Exception.class)
        public ResponseEntity<ApiError> inesperado(Exception ex, HttpServletRequest req) {
            log.error("Error inesperado", ex);
            return responder(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno del servidor", req);
        }

        private ResponseEntity<ApiError> responder(HttpStatus estado, String mensaje, HttpServletRequest req) {
            return ResponseEntity.status(estado).body(new ApiError(LocalDateTime.now(), estado.value(),
                    estado.getReasonPhrase(), mensaje, req.getRequestURI()));
        }
    }
}
