package com.libros.gestion_cliente.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CrearFamiliarRequest {
    @NotNull(message = "El ID del cliente es obligatorio")
    private Long clienteId;

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @NotBlank(message = "El apellido es obligatorio")
    private String apellido;

    @NotBlank(message = "La relación es obligatoria")
    private String relacion; // Ej: "Hijo", "Esposa"

    private Integer anioNacimiento;
    private String intereses;
}