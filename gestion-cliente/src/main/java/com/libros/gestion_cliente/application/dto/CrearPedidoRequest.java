package com.libros.gestion_cliente.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CrearPedidoRequest {
    @NotNull(message = "El ID del cliente es obligatorio")
    private Long clienteId;

    @NotBlank(message = "La descripción del libro es obligatoria")
    private String descripcion; // Ej: "Harry Potter Edición 20 aniversario"
}