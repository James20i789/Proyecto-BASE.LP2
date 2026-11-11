package pe.edu.upeu.lp2_gp2.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    // 1. Manejar nuestra excepción personalizada (404 Not Found)
    @ExceptionHandler(ResouceNotFoundException.class)
    public ResponseEntity<Object> handleResourceNotFound(ResouceNotFoundException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.NOT_FOUND.value());
        body.put("error", "Not Found");
        body.put("message", ex.getMessage());

        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
    }

    // 2. Manejar errores de validación (400 Bad Request)
    // Esto atrapa los errores de @Valid en tus DTOs - VALIDAR MÉTODOS
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Bad Request");

        // Recolectamos todos los errores de los campos
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );
        body.put("errors", errors);

        // RETORNA LA RESPUESTA DE LA ENTIDAD
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    // 3. Manejar cualquier otro error inesperado (500 Internal Server Error)
    // EXCEPTION.HANDLER - ENTITY | CLASES GLOBALES
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGlobalException(Exception ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        body.put("message", "Ocurrió un error interno en el servidor");
        body.put("details", ex.getMessage());

        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    // EXCEPTION.HANDLER - ENTITY | DUPLICAR RECURSOS
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<Object> handleDuplicateResource(DuplicateResourceException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.CONFLICT.value());
        body.put("error", "Conflict");
        body.put("message", ex.getMessage());

        return new ResponseEntity<>(body, HttpStatus.CONFLICT);
    }

    // EXCEPTION.HANDLER - ENTITY | INSUFICIENTE STOCK
    @ExceptionHandler(InsufficienteStockException.class)
    public ResponseEntity<Object> handleInsufficientStock(InsufficienteStockException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Stock Error");
        body.put("message", ex.getMessage());

        // RETORNAR RESPUESTA SEGÚN EL ESTADO DE LA ENTIDAD
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }



}