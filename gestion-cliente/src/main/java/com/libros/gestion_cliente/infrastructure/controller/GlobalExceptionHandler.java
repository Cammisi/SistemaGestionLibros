package com.libros.gestion_cliente.infrastructure.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException; // <--- Importante
import org.springframework.web.bind.MethodArgumentNotValidException; // <--- Importante
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Capturamos AMBOS tipos de errores de lógica
    @ExceptionHandler({IllegalStateException.class, IllegalArgumentException.class})
    public ResponseEntity<Map<String, Object>> handleBusinessRules(RuntimeException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, "Error de Negocio", ex.getMessage());
    }

    // 2. Error de Validación (@Valid falló) -> 400
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        StringBuilder errores = new StringBuilder();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errores.append(error.getField()).append(": ").append(error.getDefaultMessage()).append("; ")
        );
        return buildResponse(HttpStatus.BAD_REQUEST, "Error de Validación", errores.toString());
    }

    // 3. Error de JSON mal formado (doble llave, coma faltante) -> 400
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleJsonError(HttpMessageNotReadableException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, "Formato JSON Inválido", "Revise la sintaxis del JSON enviado.");
    }

    // 4. Error General (NullPointer, SQL falló, etc) -> 500
    // Ojo: RuntimeException es muy genérico. Lo ideal es capturar EntityNotFoundException para 404.
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntime(RuntimeException ex) {
        // Si el mensaje dice explícitamente "no encontrado", devolvemos 404 (parche rápido)
        if (ex.getMessage() != null && ex.getMessage().contains("no encontrado")) {
            return buildResponse(HttpStatus.NOT_FOUND, "Recurso no encontrado", ex.getMessage());
        }
        // Para todo lo demás, es un error interno del servidor
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Error Interno", ex.getMessage());
    }

    // Método auxiliar para no repetir código
    private ResponseEntity<Map<String, Object>> buildResponse(HttpStatus status, String error, String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status.value());
        body.put("error", error);
        body.put("message", message);
        return ResponseEntity.status(status).body(body);
    }
}