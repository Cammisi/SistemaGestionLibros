package com.libros.gestion_cliente.application.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.util.List;

@Data// Getters, Setters, ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CrearVentaRequest {

    @NotNull
    private Long clienteId;

    @NotNull
    private Integer cantidadCuotas;

    @NotEmpty(message = "La venta debe tener al menos un libro")
    @Valid
    private List<CrearDetalleVentaRequest> detalles;
}