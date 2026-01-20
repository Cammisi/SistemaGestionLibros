package com.libros.gestion_cliente.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ReporteItem {
    private String etiqueta; // Nombre del libro o cliente
    private Number valor;    // Cantidad o monto
}