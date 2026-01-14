package com.libros.gestion_cliente.application.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data // Getters, Setters, ToString
public class CrearVentaRequest {

    @NotNull
    private Long clienteId;

    @NotEmpty // Debe haber al menos un libro
    private List<ItemVenta> items;

    @NotNull
    private Integer cantidadCuotas;

    @Data
    public static class ItemVenta {
        @NotNull
        private Long libroId;
        @NotNull
        private Integer cantidad;
    }
}