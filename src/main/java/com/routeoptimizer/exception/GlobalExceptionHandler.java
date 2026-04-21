package com.routeoptimizer.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import java.util.Map;
import java.util.HashMap;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException ex) {
        Map<String, String> error = new java.util.HashMap<>();
        error.put("error", ex.getMessage());
        error.put("message", ex.getMessage()); // Ensure frontend grabs it exactly
        
        // Lógica de Caliche: si el mensaje contiene "not found", retornamos 404
        if (ex.getMessage() != null && (ex.getMessage().toLowerCase().contains("not found") || ex.getMessage().contains("no route"))) {
            return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
        }
        
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        StringBuilder errorMessage = new StringBuilder("Por favor corrige lo siguiente: ");
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            errorMessage.append(error.getDefaultMessage()).append(". ");
        });

        Map<String, String> response = new HashMap<>();
        response.put("error", "Validación Fallida");
        response.put("message", errorMessage.toString().trim());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(org.springframework.security.core.AuthenticationException.class)
    public ResponseEntity<?> handleAuthenticationException(org.springframework.security.core.AuthenticationException e) {
        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body(Map.of("error", "Credenciales incorrectas", "message", e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGeneralException(Exception e) {
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(Map.of("error", "Ha ocurrido un error inesperado en el servidor"));
    }
}
