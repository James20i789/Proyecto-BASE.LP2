package pe.edu.upeu.lp2_gp2.exceptions;

import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.AccountStatusException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger LOG = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> validation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.putIfAbsent(error.getField(), error.getDefaultMessage()));
        return ResponseEntity.badRequest().body(Map.of(
                "message", "Revisa los campos enviados.", "errors", errors));
    }

    @ExceptionHandler({BadCredentialsException.class, AccountStatusException.class})
    public ResponseEntity<?> authentication() {
        return message(HttpStatus.UNAUTHORIZED, "Usuario o contraseña incorrectos.");
    }

    @ExceptionHandler({DuplicateResourceException.class, DataIntegrityViolationException.class})
    public ResponseEntity<?> conflict() {
        return message(HttpStatus.CONFLICT, "La operación entra en conflicto con un registro existente.");
    }

    @ExceptionHandler(InsufficienteStockException.class)
    public ResponseEntity<?> stock(InsufficienteStockException ex) {
        return message(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(ResouceNotFoundException.class)
    public ResponseEntity<?> notFound(ResouceNotFoundException ex) {
        return message(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<?> resource() {
        return message(HttpStatus.NOT_FOUND, "Recurso no encontrado.");
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<?> invalidBody() {
        return message(HttpStatus.BAD_REQUEST, "Los datos enviados tienen un formato inválido.");
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<?> status(ResponseStatusException ex) {
        return message(ex.getStatusCode(), ex.getReason());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> unexpected(Exception ex) {
        LOG.error("Error inesperado al procesar la solicitud", ex);
        return message(HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrió un error interno.");
    }

    private ResponseEntity<?> message(HttpStatusCode status, String message) {
        return ResponseEntity.status(status).body(Map.of(
                "status", status.value(), "message", message == null ? "Error en la solicitud." : message));
    }
}
