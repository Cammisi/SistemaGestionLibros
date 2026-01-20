package com.libros.gestion_cliente.application.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CrearDetalleVentaRequest {

    @NotNull
    private Long libroId;

    @NotNull
    @Positive
    private Integer cantidad;
}