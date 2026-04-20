package com.routeoptimizer.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getMessage());
        error.put("status", "404");
        
        // Si el mensaje contiene "not found", retornamos 404, sino 500 controlado
        if (ex.getMessage().toLowerCase().contains("not found") || ex.getMessage().contains("no route")) {
            return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
        }
        
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
