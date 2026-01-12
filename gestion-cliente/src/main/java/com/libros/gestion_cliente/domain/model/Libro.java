package com.libros.gestion_cliente.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "libros")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Libro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El título es obligatorio")
    @Column(nullable = false)
    private String titulo;

    @NotBlank(message = "El autor es obligatorio")
    @Column(nullable = false)
    private String autor;

    @NotBlank(message = "El ISBN es obligatorio")
    @Column(unique = true, nullable = false, length = 20)
    private String isbn;

    // Lo dejamos opcional (sin @NotBlank) por si decides no usarlo
    private String editorial;

    // NUEVO CAMPO: Cantidad de volúmenes que componen este ítem
    @NotNull(message = "La cantidad de volúmenes es obligatoria")
    @Min(value = 1, message = "Debe tener al menos 1 volumen")
    @Column(name = "cantidad_volumenes", nullable = false)
    @Builder.Default // Por defecto es 1 si no se especifica
    private Integer cantidadVolumenes = 1;

    // IMPORTANTE: BigDecimal para el precio
    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.01", message = "El precio debe ser mayor a 0")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precio;

    // Stock de inventario (cuántas copias físicas tienes para vender)
    @NotNull(message = "El stock es obligatorio")
    @Min(value = 0, message = "El stock no puede ser negativo")
    @Column(nullable = false)
    private Integer stock;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    private String genero;
}